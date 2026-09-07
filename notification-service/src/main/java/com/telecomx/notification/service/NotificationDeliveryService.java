package com.telecomx.notification.service;

import com.telecomx.notification.domain.NotificationChannel;
import com.telecomx.notification.domain.NotificationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    // Bounded, most-recent-first in-memory log purely for the admin dashboard demo.
    private final CopyOnWriteArrayList<NotificationRecord> recent = new CopyOnWriteArrayList<>();
    private static final int MAX_RECORDS = 200;

    public void send(Long customerId, NotificationChannel channel, String subject, String message) {
        // Real implementation would call an SMS gateway / email provider (Twilio, SES, etc).
        // Simulated here so the platform is runnable without external credentials.
        log.info("[{} -> customer {}] {}: {}", channel, customerId, subject, message);

        NotificationRecord record = NotificationRecord.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .channel(channel)
                .subject(subject)
                .message(message)
                .sentAt(Instant.now())
                .build();

        recent.add(0, record);
        while (recent.size() > MAX_RECORDS) {
            recent.remove(recent.size() - 1);
        }
    }

    public List<NotificationRecord> getRecent() {
        return List.copyOf(recent);
    }
}
