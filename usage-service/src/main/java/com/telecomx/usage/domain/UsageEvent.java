package com.telecomx.usage.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Deliberately loose/semi-structured: a CALL event has durationSeconds, an SMS event
 * doesn't, a DATA event has dataMb instead. Modeling this as one rigid relational
 * table would mean a lot of nullable columns or three separate tables with joins for
 * something that's fundamentally just "an append-only stream of usage facts" -
 * exactly the shape MongoDB documents are good at.
 */
@Document(collection = "usage_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsageEvent {

    @Id
    private String id;

    @Indexed
    private Long customerId;

    @Indexed
    private String msisdn;

    private UsageType type;

    private Integer durationSeconds; // CALL
    private Double dataMb;           // DATA
    private String destinationNumber; // CALL / SMS

    @Indexed
    private Instant recordedAt;

    @Builder.Default
    private boolean billed = false;
}
