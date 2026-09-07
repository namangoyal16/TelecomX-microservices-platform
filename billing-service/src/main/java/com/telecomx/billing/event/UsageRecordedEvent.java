package com.telecomx.billing.event;

public record UsageRecordedEvent(
        String usageEventId, Long customerId, String msisdn, String type,
        Integer durationSeconds, Double dataMb
) {}
