package com.telecomx.notification.event;

public record SubscriberProvisionedEvent(Long subscriptionId, Long customerId, String planCode, String msisdn) {}
