package com.telecomx.provisioning.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Retry + Dead-Letter strategy shared by all Kafka consumers in this service.
 *
 * Why: Kafka delivers at-least-once, and downstream dependencies (DB, Redis) can be
 * momentarily unavailable. Instead of either (a) blocking the partition forever on a
 * poison message, or (b) silently dropping failed messages, we:
 *   1. Retry with exponential backoff for transient failures (up to 4 attempts).
 *   2. After retries are exhausted, publish the message to a ".DLT" topic instead of
 *      losing it, so ops can inspect + manually replay once the root cause is fixed.
 */
@Configuration
public class KafkaRetryConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        var recoverer = new DeadLetterPublishingRecoverer(template,
                (ConsumerRecord<?, ?> record, Exception ex) ->
                        new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition()));

        // 1s initial interval, x2 multiplier, capped at 10s, max 4 attempts
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(15000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
