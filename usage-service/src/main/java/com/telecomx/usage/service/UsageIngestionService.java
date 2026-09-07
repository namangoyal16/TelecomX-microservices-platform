package com.telecomx.usage.service;

import com.telecomx.usage.domain.UsageEvent;
import com.telecomx.usage.dto.UsageIngestRequest;
import com.telecomx.usage.event.UsageEventPublisher;
import com.telecomx.usage.event.UsageRecordedEvent;
import com.telecomx.usage.repository.UsageEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UsageIngestionService {

    private final UsageEventRepository repository;
    private final UsageEventPublisher publisher;

    public UsageIngestionService(UsageEventRepository repository, UsageEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public UsageEvent ingest(UsageIngestRequest req) {
        UsageEvent event = UsageEvent.builder()
                .customerId(req.customerId())
                .msisdn(req.msisdn())
                .type(req.type())
                .durationSeconds(req.durationSeconds())
                .dataMb(req.dataMb())
                .destinationNumber(req.destinationNumber())
                .recordedAt(Instant.now())
                .billed(false)
                .build();

        // Write path is intentionally fast: persist to Mongo, then fire-and-forget the
        // Kafka event. Billing aggregation happens asynchronously and independently.
        event = repository.save(event);
        publisher.publish(new UsageRecordedEvent(
                event.getId(), event.getCustomerId(), event.getMsisdn(),
                event.getType().name(), event.getDurationSeconds(), event.getDataMb()));
        return event;
    }
}
