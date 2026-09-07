package com.telecomx.usage.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UsageEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UsageEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public UsageEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${topics.usage-recorded}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(UsageRecordedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.customerId()), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish usage.recorded for customer {}", event.customerId(), ex);
                });
    }
}
