package com.telecomx.provisioning.kafka;

import com.telecomx.provisioning.event.PaymentFailedEvent;
import com.telecomx.provisioning.service.ProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Cross-service side effect: after repeated billing failures, Provisioning suspends
 * the subscriber's service. This is intentionally async via Kafka rather than Billing
 * calling Provisioning synchronously, so a billing-cycle job isn't coupled to
 * provisioning's availability/latency.
 */
@Component
public class PaymentFailedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentFailedListener.class);
    private static final int SUSPEND_THRESHOLD = 3;

    private final ProvisioningService provisioningService;

    public PaymentFailedListener(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @KafkaListener(topics = "${topics.payment-failed}", groupId = "provisioning-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.warn("payment.failed received for customerId={} consecutiveFailures={}",
                event.customerId(), event.consecutiveFailures());
        if (event.consecutiveFailures() >= SUSPEND_THRESHOLD) {
            provisioningService.suspend(event.customerId());
        }
    }
}
