package com.telecomx.provisioning.event;

public record SubscriptionRequestedEvent(
        Long subscriptionId, Long customerId, String planCode, String customerPhoneNumber
) {}
