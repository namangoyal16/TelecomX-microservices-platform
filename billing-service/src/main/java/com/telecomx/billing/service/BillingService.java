package com.telecomx.billing.service;

import com.telecomx.billing.domain.*;
import com.telecomx.billing.event.*;
import com.telecomx.billing.exception.ResourceNotFoundException;
import com.telecomx.billing.repository.BillingProfileRepository;
import com.telecomx.billing.repository.InvoiceRepository;
import com.telecomx.billing.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private static final Random RANDOM = new Random();

    private final BillingProfileRepository profileRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PlanPricing planPricing;
    private final BillingEventPublisher eventPublisher;
    private final int suspendThreshold;

    public BillingService(BillingProfileRepository profileRepository, InvoiceRepository invoiceRepository,
                           PaymentRepository paymentRepository, PlanPricing planPricing,
                           BillingEventPublisher eventPublisher,
                           @Value("${billing.max-consecutive-payment-failures-before-suspend-signal}") int suspendThreshold) {
        this.profileRepository = profileRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.planPricing = planPricing;
        this.eventPublisher = eventPublisher;
        this.suspendThreshold = suspendThreshold;
    }

    /** Consumes subscriber.provisioned -> opens a billing profile so usage can start accruing. */
    @Transactional
    public void onSubscriberProvisioned(SubscriberProvisionedEvent event) {
        if (profileRepository.findByCustomerId(event.customerId()).isPresent()) {
            log.info("Billing profile for customer {} already exists - ignoring duplicate event", event.customerId());
            return;
        }
        BillingProfile profile = BillingProfile.builder()
                .customerId(event.customerId())
                .msisdn(event.msisdn())
                .planCode(event.planCode())
                .monthlyPrice(planPricing.monthlyPriceFor(event.planCode()))
                .unbilledUsageCharge(BigDecimal.ZERO)
                .build();
        profileRepository.save(profile);
        log.info("Opened billing profile for customerId={} plan={}", event.customerId(), event.planCode());
    }

    /** Consumes usage.recorded -> accrues charge onto the customer's running total. */
    @Transactional
    public void onUsageRecorded(UsageRecordedEvent event) {
        BillingProfile profile = profileRepository.findWithLockByCustomerId(event.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No billing profile for customer " + event.customerId() + " - usage event dropped to DLT for investigation"));

        BigDecimal charge = planPricing.chargeForUsage(event.type(), event.durationSeconds(), event.dataMb());
        profile.setUnbilledUsageCharge(profile.getUnbilledUsageCharge().add(charge));
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }

    /** Generates an invoice from plan charge + all unbilled usage, then resets the running total. */
    @Transactional
    public Invoice generateInvoice(Long customerId) {
        BillingProfile profile = profileRepository.findWithLockByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No billing profile for customer " + customerId));

        BigDecimal planCharge = profile.getMonthlyPrice();
        BigDecimal usageCharge = profile.getUnbilledUsageCharge();
        BigDecimal total = planCharge.add(usageCharge);

        Invoice invoice = Invoice.builder()
                .customerId(customerId)
                .planCharge(planCharge)
                .usageCharge(usageCharge)
                .totalAmount(total)
                .status(InvoiceStatus.PENDING)
                .build();
        invoice = invoiceRepository.save(invoice);

        profile.setUnbilledUsageCharge(BigDecimal.ZERO);
        profileRepository.save(profile);

        eventPublisher.publishInvoiceGenerated(new InvoiceGeneratedEvent(invoice.getId(), customerId, total));
        log.info("Generated invoice {} for customer {} total={}", invoice.getId(), customerId, total);
        return invoice;
    }

    /**
     * Processes payment for an invoice. Idempotency (see IdempotencyService / controller)
     * ensures this business logic only ever runs once per Idempotency-Key even if the
     * HTTP request is retried by the client after a timeout.
     */
    @Transactional
    public Payment payInvoice(Long customerId, Long invoiceId, boolean simulateFailure) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        if (!invoice.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Invoice does not belong to customer " + customerId);
        }

        BillingProfile profile = profileRepository.findWithLockByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No billing profile for customer " + customerId));

        boolean success = !simulateFailure;

        Payment payment = Payment.builder()
                .invoiceId(invoiceId)
                .customerId(customerId)
                .amount(invoice.getTotalAmount())
                .status(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .build();
        payment = paymentRepository.save(payment);

        if (success) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            profile.setConsecutivePaymentFailures(0);
        } else {
            invoice.setStatus(InvoiceStatus.PAYMENT_FAILED);
            profile.setConsecutivePaymentFailures(profile.getConsecutivePaymentFailures() + 1);

            eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    customerId, invoiceId, profile.getConsecutivePaymentFailures()));

            if (profile.getConsecutivePaymentFailures() >= suspendThreshold) {
                log.warn("Customer {} has {} consecutive payment failures - suspend signal sent",
                        customerId, profile.getConsecutivePaymentFailures());
            }
        }

        invoiceRepository.save(invoice);
        profileRepository.save(profile);

        // IMPORTANT: we deliberately do NOT throw here even though payment failed.
        // Throwing inside this @Transactional method would roll back the failure
        // record we just wrote (invoice -> PAYMENT_FAILED, profile counters) - and
        // that audit trail is exactly what billing needs to keep. The controller
        // inspects payment.getStatus() and maps FAILED to HTTP 402 itself.
        return payment;
    }

    public List<Invoice> getInvoicesForCustomer(Long customerId) {
        return invoiceRepository.findByCustomerIdOrderByGeneratedAtDesc(customerId);
    }
}
