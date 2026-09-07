package com.telecomx.provisioning.service;

import com.telecomx.provisioning.domain.ProvisioningRecord;
import com.telecomx.provisioning.domain.ProvisioningStatus;
import com.telecomx.provisioning.event.ProvisioningEventPublisher;
import com.telecomx.provisioning.event.SubscriptionRequestedEvent;
import com.telecomx.provisioning.repository.ProvisioningRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies the idempotency guarantee described in the README: replaying the same
 * subscription.requested Kafka event (as happens under at-least-once delivery)
 * must NOT allocate a second MSISDN or create a duplicate provisioning record.
 */
@ExtendWith(MockitoExtension.class)
class ProvisioningServiceTest {

    @Mock ProvisioningRecordRepository repository;
    @Mock SubscriberCacheService cacheService;
    @Mock ProvisioningEventPublisher eventPublisher;

    @InjectMocks ProvisioningService provisioningService;

    @Test
    void activate_isIdempotent_onDuplicateEvent() {
        SubscriptionRequestedEvent event = new SubscriptionRequestedEvent(100L, 1L, "PLUS_5G", "9990001111");

        ProvisioningRecord existingRecord = ProvisioningRecord.builder()
                .id(1L).subscriptionId(100L).customerId(1L).planCode("PLUS_5G")
                .msisdn("9123456789").status(ProvisioningStatus.ACTIVE).build();

        when(repository.findBySubscriptionId(100L)).thenReturn(Optional.of(existingRecord));

        provisioningService.activate(event);
        provisioningService.activate(event); // simulate Kafka redelivery

        // save() should NEVER be called because the record already existed both times
        verify(repository, never()).save(any());
        // but the confirmation event is still re-published both times (safe, consumers are idempotent too)
        verify(eventPublisher, times(2)).publishProvisioned(any());
    }

    @Test
    void activate_createsNewRecord_whenNotSeenBefore() {
        SubscriptionRequestedEvent event = new SubscriptionRequestedEvent(200L, 2L, "BASIC_5G", "9990002222");
        when(repository.findBySubscriptionId(200L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> {
            ProvisioningRecord r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });

        provisioningService.activate(event);

        verify(repository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishProvisioned(any());
    }
}
