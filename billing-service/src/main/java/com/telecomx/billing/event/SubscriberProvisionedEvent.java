package com.telecomx.billing.event;

public record SubscriberProvisionedEvent(Long subscriptionId, Long customerId, String planCode, String msisdn) {}
