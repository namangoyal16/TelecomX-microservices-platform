package com.telecomx.provisioning.kafka;

import com.telecomx.provisioning.event.SubscriptionRequestedEvent;
import com.telecomx.provisioning.service.ProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionRequestedListener.class);

    private final ProvisioningService provisioningService;

    public SubscriptionRequestedListener(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @KafkaListener(topics = "${topics.subscription-requested}", groupId = "provisioning-service")
    public void onSubscriptionRequested(SubscriptionRequestedEvent event) {
        log.info("Received subscription.requested subscriptionId={} customerId={}",
                event.subscriptionId(), event.customerId());
        // Any exception here is caught by KafkaRetryConfig's DefaultErrorHandler:
        // retried with backoff, then routed to subscription.requested.DLT if it keeps failing.
        provisioningService.activate(event);
    }
}
