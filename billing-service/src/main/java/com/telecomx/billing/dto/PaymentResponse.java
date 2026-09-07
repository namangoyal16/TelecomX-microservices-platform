package com.telecomx.billing.dto;

import com.telecomx.billing.domain.Payment;
import java.math.BigDecimal;

public record PaymentResponse(Long id, Long invoiceId, BigDecimal amount, String status) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getInvoiceId(), p.getAmount(), p.getStatus().name());
    }
}
