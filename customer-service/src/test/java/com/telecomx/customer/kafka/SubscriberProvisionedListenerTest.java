package com.telecomx.customer.kafka;

import com.telecomx.customer.domain.Subscription;
import com.telecomx.customer.domain.SubscriptionStatus;
import com.telecomx.customer.event.SubscriberProvisionedEvent;
import com.telecomx.customer.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is the test that would have caught the "status stuck on PENDING forever" bug:
 * it proves the subscription record actually gets flipped to ACTIVE once the
 * subscriber.provisioned event is consumed.
 */
@ExtendWith(MockitoExtension.class)
class SubscriberProvisionedListenerTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @InjectMocks SubscriberProvisionedListener listener;

    @Test
    void onSubscriberProvisioned_marksSubscriptionActiveWithMsisdn() {
        Subscription subscription = Subscription.builder()
                .id(1L).customerId(10L).planId(2L).status(SubscriptionStatus.PENDING).build();
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        listener.onSubscriberProvisioned(new SubscriberProvisionedEvent(1L, 10L, "PLUS_5G", "9123456789"));

        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getMsisdn()).isEqualTo("9123456789");
        assertThat(subscription.getActivatedAt()).isNotNull();
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void onSubscriberProvisioned_doesNotThrow_whenSubscriptionUnknown() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        listener.onSubscriberProvisioned(new SubscriberProvisionedEvent(999L, 10L, "PLUS_5G", "9123456789"));

        verify(subscriptionRepository, org.mockito.Mockito.never()).save(any());
    }
}
