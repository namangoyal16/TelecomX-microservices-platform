package com.telecomx.usage.dto;

import com.telecomx.usage.domain.UsageType;
import jakarta.validation.constraints.NotNull;

public record UsageIngestRequest(
        @NotNull Long customerId,
        @NotNull String msisdn,
        @NotNull UsageType type,
        Integer durationSeconds,
        Double dataMb,
        String destinationNumber
) {}
