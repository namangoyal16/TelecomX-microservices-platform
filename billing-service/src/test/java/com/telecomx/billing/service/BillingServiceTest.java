package com.telecomx.billing.service;

import com.telecomx.billing.domain.*;
import com.telecomx.billing.event.BillingEventPublisher;
import com.telecomx.billing.event.UsageRecordedEvent;
import com.telecomx.billing.repository.BillingProfileRepository;
import com.telecomx.billing.repository.InvoiceRepository;
import com.telecomx.billing.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock BillingProfileRepository profileRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PlanPricing planPricing;
    @Mock BillingEventPublisher eventPublisher;

    BillingService billingService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        billingService = new BillingService(profileRepository, invoiceRepository, paymentRepository,
                planPricing, eventPublisher, 3);
    }

    @Test
    void onUsageRecorded_accruesChargeOntoBillingProfile() {
        BillingProfile profile = BillingProfile.builder()
                .customerId(1L).msisdn("9123456789").planCode("PLUS_5G")
                .monthlyPrice(new BigDecimal("599.00")).unbilledUsageCharge(BigDecimal.ZERO).build();

        when(profileRepository.findWithLockByCustomerId(1L)).thenReturn(Optional.of(profile));
        when(planPricing.chargeForUsage("CALL", 120, null)).thenReturn(new BigDecimal("2.00"));

        billingService.onUsageRecorded(new UsageRecordedEvent("evt1", 1L, "9123456789", "CALL", 120, null));

        assertThat(profile.getUnbilledUsageCharge()).isEqualByComparingTo("2.00");
        verify(profileRepository).save(profile);
    }

    @Test
    void payInvoice_doesNotThrow_onFailure_soFailureIsRecordedNotRolledBack() {
        Invoice invoice = Invoice.builder().id(10L).customerId(1L).totalAmount(new BigDecimal("599.00"))
                .status(InvoiceStatus.PENDING).build();
        BillingProfile profile = BillingProfile.builder()
                .customerId(1L).monthlyPrice(new BigDecimal("599.00"))
                .unbilledUsageCharge(BigDecimal.ZERO).consecutivePaymentFailures(0).build();

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(profileRepository.findWithLockByCustomerId(1L)).thenReturn(Optional.of(profile));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = billingService.payInvoice(1L, 10L, true); // simulateFailure = true

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(profile.getConsecutivePaymentFailures()).isEqualTo(1);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAYMENT_FAILED);
        verify(eventPublisher).publishPaymentFailed(any());
    }
}
