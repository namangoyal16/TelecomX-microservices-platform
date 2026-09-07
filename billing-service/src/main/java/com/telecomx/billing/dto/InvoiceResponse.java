package com.telecomx.billing.dto;

import com.telecomx.billing.domain.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id, Long customerId, BigDecimal planCharge, BigDecimal usageCharge,
        BigDecimal totalAmount, String status, LocalDateTime generatedAt
) {
    public static InvoiceResponse from(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getCustomerId(), i.getPlanCharge(), i.getUsageCharge(),
                i.getTotalAmount(), i.getStatus().name(), i.getGeneratedAt());
    }
}
