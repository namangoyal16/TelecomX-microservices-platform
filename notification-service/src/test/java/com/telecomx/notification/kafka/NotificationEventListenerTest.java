package com.telecomx.notification.kafka;

import com.telecomx.notification.domain.NotificationChannel;
import com.telecomx.notification.event.SubscriberProvisionedEvent;
import com.telecomx.notification.service.NotificationDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock NotificationDeliveryService deliveryService;
    @InjectMocks NotificationEventListener listener;

    @Test
    void onSubscriberProvisioned_sendsSmsWelcomeMessage() {
        listener.onSubscriberProvisioned(new SubscriberProvisionedEvent(1L, 100L, "PLUS_5G", "9123456789"));

        verify(deliveryService).send(eq(100L), eq(NotificationChannel.SMS), any(), any());
    }
}
