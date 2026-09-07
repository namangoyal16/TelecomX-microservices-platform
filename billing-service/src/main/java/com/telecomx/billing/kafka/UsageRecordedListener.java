package com.telecomx.billing.kafka;

import com.telecomx.billing.event.UsageRecordedEvent;
import com.telecomx.billing.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UsageRecordedListener {
    private static final Logger log = LoggerFactory.getLogger(UsageRecordedListener.class);
    private final BillingService billingService;

    public UsageRecordedListener(BillingService billingService) {
        this.billingService = billingService;
    }

    // Handles the highest-volume topic in the platform - peak-hour call/SMS/data
    // events all flow through here. Errors are retried + DLT'd (KafkaRetryConfig)
    // rather than blocking the partition or silently dropping revenue-bearing events.
    @KafkaListener(topics = "${topics.usage-recorded}", groupId = "billing-service")
    public void onUsageRecorded(UsageRecordedEvent event) {
        billingService.onUsageRecorded(event);
    }
}
