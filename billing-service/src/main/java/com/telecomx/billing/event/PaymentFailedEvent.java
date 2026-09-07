package com.telecomx.billing.event;

public record PaymentFailedEvent(Long customerId, Long invoiceId, int consecutiveFailures) {}
