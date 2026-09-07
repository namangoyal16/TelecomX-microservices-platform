package com.telecomx.notification.domain;

import lombok.*;
import java.time.Instant;

/**
 * In-memory delivery log only (no DB) - by design this service holds no persistent
 * state of its own. If it restarts, it simply resumes consuming from Kafka at its
 * last committed offset; no notification data is "lost" in a way that matters,
 * since the source of truth for what HAPPENED (provisioning, billing) lives in
 * those services' own databases.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationRecord {
    private String id;
    private Long customerId;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private Instant sentAt;
}
