package com.telecomx.usage.repository;

import com.telecomx.usage.domain.UsageEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface UsageEventRepository extends MongoRepository<UsageEvent, String> {
    List<UsageEvent> findByCustomerIdAndRecordedAtBetween(Long customerId, Instant from, Instant to);
    List<UsageEvent> findByCustomerIdOrderByRecordedAtDesc(Long customerId);
}
