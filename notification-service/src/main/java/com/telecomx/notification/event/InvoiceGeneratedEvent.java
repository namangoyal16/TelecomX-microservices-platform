package com.telecomx.notification.event;

import java.math.BigDecimal;

public record InvoiceGeneratedEvent(Long invoiceId, Long customerId, BigDecimal totalAmount) {}
