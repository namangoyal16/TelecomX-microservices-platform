package com.telecomx.provisioning.service;

import com.telecomx.provisioning.domain.ProvisioningRecord;
import com.telecomx.provisioning.domain.ProvisioningStatus;
import com.telecomx.provisioning.dto.SubscriberStatusCache;
import com.telecomx.provisioning.event.ProvisioningEventPublisher;
import com.telecomx.provisioning.event.SubscriberProvisionedEvent;
import com.telecomx.provisioning.event.SubscriptionRequestedEvent;
import com.telecomx.provisioning.exception.ResourceNotFoundException;
import com.telecomx.provisioning.repository.ProvisioningRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class ProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProvisioningRecordRepository repository;
    private final SubscriberCacheService cacheService;
    private final ProvisioningEventPublisher eventPublisher;

    public ProvisioningService(ProvisioningRecordRepository repository, SubscriberCacheService cacheService,
                                ProvisioningEventPublisher eventPublisher) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Activates a subscriber. Idempotent at the DB level via the unique constraint on
     * subscription_id: if this event is redelivered by Kafka, the second attempt finds
     * the existing record and simply re-emits the confirmation event instead of
     * allocating a second MSISDN.
     */
    @Transactional
    public void activate(SubscriptionRequestedEvent event) {
        var existing = repository.findBySubscriptionId(event.subscriptionId());
        ProvisioningRecord record;
        if (existing.isPresent()) {
            log.info("Subscription {} already provisioned - skipping duplicate activation (idempotent)", event.subscriptionId());
            record = existing.get();
        } else {
            String msisdn = generateMsisdn();
            record = ProvisioningRecord.builder()
                    .subscriptionId(event.subscriptionId())
                    .customerId(event.customerId())
                    .planCode(event.planCode())
                    .msisdn(msisdn)
                    .status(ProvisioningStatus.ACTIVE)
                    .build();
            record = repository.save(record);
            log.info("Provisioned subscriptionId={} customerId={} msisdn={}",
                    event.subscriptionId(), event.customerId(), msisdn);
        }

        cacheService.put(record.getCustomerId(), new SubscriberStatusCache(
                record.getCustomerId(), record.getMsisdn(), record.getPlanCode(), record.getStatus().name()));

        eventPublisher.publishProvisioned(new SubscriberProvisionedEvent(
                record.getSubscriptionId(), record.getCustomerId(), record.getPlanCode(), record.getMsisdn()));
    }

    @Transactional
    public void suspend(Long customerId) {
        ProvisioningRecord record = repository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No provisioning record for customer " + customerId));
        record.setStatus(ProvisioningStatus.SUSPENDED);
        record.setSuspendedAt(java.time.LocalDateTime.now());
        repository.save(record);
        cacheService.evict(customerId); // stale "ACTIVE" cache entry must not linger
        log.warn("Suspended provisioning for customerId={} (e.g. after repeated payment failure)", customerId);
    }

    @Transactional
    public void reactivate(Long customerId) {
        ProvisioningRecord record = repository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No provisioning record for customer " + customerId));
        record.setStatus(ProvisioningStatus.ACTIVE);
        record.setSuspendedAt(null);
        repository.save(record);
        cacheService.put(customerId, new SubscriberStatusCache(
                customerId, record.getMsisdn(), record.getPlanCode(), "ACTIVE"));
    }

    public ProvisioningRecord getByCustomer(Long customerId) {
        return repository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No provisioning record for customer " + customerId));
    }

    private String generateMsisdn() {
        StringBuilder sb = new StringBuilder("9");
        for (int i = 0; i < 9; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }
}
