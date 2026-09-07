package com.telecomx.provisioning.event;

/** Published once a subscriber is successfully activated. Billing starts metering
 *  on this signal; Notification sends the "you're connected" message. */
public record SubscriberProvisionedEvent(
        Long subscriptionId, Long customerId, String planCode, String msisdn
) {}
