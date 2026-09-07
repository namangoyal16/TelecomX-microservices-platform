package com.telecomx.billing.kafka;

import com.telecomx.billing.event.SubscriberProvisionedEvent;
import com.telecomx.billing.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriberProvisionedListener {
    private static final Logger log = LoggerFactory.getLogger(SubscriberProvisionedListener.class);
    private final BillingService billingService;

    public SubscriberProvisionedListener(BillingService billingService) {
        this.billingService = billingService;
    }

    @KafkaListener(topics = "${topics.subscriber-provisioned}", groupId = "billing-service")
    public void onSubscriberProvisioned(SubscriberProvisionedEvent event) {
        log.info("Received subscriber.provisioned customerId={}", event.customerId());
        billingService.onSubscriberProvisioned(event);
    }
}
