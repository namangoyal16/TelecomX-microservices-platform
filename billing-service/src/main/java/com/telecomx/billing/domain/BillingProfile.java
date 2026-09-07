package com.telecomx.billing.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillingProfile {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private String msisdn;

    @Column(name = "plan_code", nullable = false)
    private String planCode;

    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "unbilled_usage_charge", nullable = false)
    @Builder.Default
    private BigDecimal unbilledUsageCharge = BigDecimal.ZERO;

    @Column(name = "consecutive_payment_failures", nullable = false)
    @Builder.Default
    private Integer consecutivePaymentFailures = 0;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
