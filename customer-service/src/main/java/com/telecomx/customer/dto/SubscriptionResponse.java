package com.telecomx.customer.dto;

import com.telecomx.customer.domain.SubscriptionStatus;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id, Long customerId, String planCode, SubscriptionStatus status,
        String msisdn, LocalDateTime requestedAt
) {}
