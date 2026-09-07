package com.telecomx.customer.event;

/** Consumed from Kafka once Provisioning Service finishes activating a subscriber.
 *  This is what closes the loop: it's how the Subscription record in THIS service's
 *  own database ever moves from PENDING to ACTIVE - without this listener, the
 *  subscription would stay PENDING forever even though provisioning succeeded,
 *  because customer-service would simply never find out. */
public record SubscriberProvisionedEvent(
        Long subscriptionId, Long customerId, String planCode, String msisdn
) {}
