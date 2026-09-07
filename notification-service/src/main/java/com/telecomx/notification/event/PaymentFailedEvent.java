package com.telecomx.notification.event;

public record PaymentFailedEvent(Long customerId, Long invoiceId, int consecutiveFailures) {}
