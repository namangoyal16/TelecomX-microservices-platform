package com.telecomx.provisioning.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProvisioningEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String provisionedTopic;

    public ProvisioningEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                       @Value("${topics.subscriber-provisioned}") String provisionedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.provisionedTopic = provisionedTopic;
    }

    public void publishProvisioned(SubscriberProvisionedEvent event) {
        kafkaTemplate.send(provisionedTopic, String.valueOf(event.customerId()), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish subscriber.provisioned for customer {}", event.customerId(), ex);
                    else log.info("Published subscriber.provisioned customerId={} msisdn={}", event.customerId(), event.msisdn());
                });
    }
}
