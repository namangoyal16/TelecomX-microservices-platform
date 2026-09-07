package com.telecomx.customer.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public SubscriptionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                       @Value("${topics.subscription-requested}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(SubscriptionRequestedEvent event) {
        // key = customerId so all events for the same customer land on the same
        // partition, preserving per-customer ordering downstream.
        kafkaTemplate.send(topic, String.valueOf(event.customerId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish subscription.requested for customer {}", event.customerId(), ex);
                    } else {
                        log.info("Published subscription.requested subscriptionId={} customerId={}",
                                event.subscriptionId(), event.customerId());
                    }
                });
    }
}
