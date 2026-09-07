package com.telecomx.billing.dto;

/** simulateFailure is a demo-only flag letting you exercise the payment-failed /
 *  suspend-signal flow on demand without wiring a real payment gateway. */
public record PaymentRequest(boolean simulateFailure) {}
