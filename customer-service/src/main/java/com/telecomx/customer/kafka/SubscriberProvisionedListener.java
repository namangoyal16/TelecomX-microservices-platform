package com.telecomx.customer.kafka;

import com.telecomx.customer.domain.SubscriptionStatus;
import com.telecomx.customer.event.SubscriberProvisionedEvent;
import com.telecomx.customer.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SubscriberProvisionedListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriberProvisionedListener.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriberProvisionedListener(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @KafkaListener(topics = "${topics.subscriber-provisioned}", groupId = "customer-service")
    @Transactional
    public void onSubscriberProvisioned(SubscriberProvisionedEvent event) {
        subscriptionRepository.findById(event.subscriptionId()).ifPresentOrElse(subscription -> {
            // Idempotent: if this event is redelivered, setting the same ACTIVE status
            // + msisdn again is harmless (no duplicate side effects).
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setMsisdn(event.msisdn());
            subscription.setActivatedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
            log.info("Subscription {} marked ACTIVE (msisdn={})", event.subscriptionId(), event.msisdn());
        }, () -> log.warn("Received subscriber.provisioned for unknown subscriptionId={}", event.subscriptionId()));
    }
}
