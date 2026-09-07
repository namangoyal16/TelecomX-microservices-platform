package com.telecomx.usage.event;

/** Published for every usage event so Billing Service can aggregate charges
 *  asynchronously, without usage ingestion ever blocking on billing's availability. */
public record UsageRecordedEvent(
        String usageEventId, Long customerId, String msisdn, String type,
        Integer durationSeconds, Double dataMb
) {}
