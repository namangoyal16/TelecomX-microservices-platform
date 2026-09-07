package com.telecomx.billing.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Small local mirror of the plan catalog's pricing, keyed by plan code.
 * Deliberately NOT a synchronous call back to Customer Service on every event -
 * Billing needs this constantly (every usage event) and plan prices change rarely,
 * so treating it as near-static reference data here avoids a chatty cross-service
 * dependency in the hot path. In a real system this would be kept in sync via a
 * "plan.updated" Kafka event; simplified here for scope.
 */
@Component
public class PlanPricing {

    private static final Map<String, BigDecimal> MONTHLY_PRICE = Map.of(
            "BASIC_5G", new BigDecimal("299.00"),
            "PLUS_5G", new BigDecimal("599.00"),
            "UNLTD_5G", new BigDecimal("999.00")
    );

    // Usage rates applied ONLY beyond what's bundled in the plan (simplified flat-rate
    // model for demo purposes - a real system would track bundle consumption too).
    private static final BigDecimal CALL_RATE_PER_MIN = new BigDecimal("0.50");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.20");
    private static final BigDecimal DATA_RATE_PER_MB = new BigDecimal("0.05");

    public BigDecimal monthlyPriceFor(String planCode) {
        return MONTHLY_PRICE.getOrDefault(planCode, new BigDecimal("299.00"));
    }

    public BigDecimal chargeForUsage(String type, Integer durationSeconds, Double dataMb) {
        return switch (type) {
            case "CALL" -> {
                double minutes = Math.ceil((durationSeconds == null ? 0 : durationSeconds) / 60.0);
                yield CALL_RATE_PER_MIN.multiply(BigDecimal.valueOf(minutes));
            }
            case "SMS" -> SMS_RATE;
            case "DATA" -> DATA_RATE_PER_MB.multiply(BigDecimal.valueOf(dataMb == null ? 0 : dataMb));
            default -> BigDecimal.ZERO;
        };
    }
}
