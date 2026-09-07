package com.telecomx.provisioning.event;

public record PaymentFailedEvent(Long customerId, Long invoiceId, int consecutiveFailures) {}
