package com.telecomx.customer.event;

/**
 * Published to Kafka topic "subscription.requested" whenever a customer picks a plan.
 * Consumed asynchronously by Provisioning Service. We do NOT call Provisioning
 * synchronously here because provisioning (SIM/number allocation) can be slow or
 * temporarily unavailable, and a customer's "subscribe" click should not hang on it.
 */
public record SubscriptionRequestedEvent(
        Long subscriptionId,
        Long customerId,
        String planCode,
        String customerPhoneNumber
) {}
