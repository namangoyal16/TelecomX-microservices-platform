package com.telecomx.provisioning.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "provisioning_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProvisioningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false, unique = true)
    private Long subscriptionId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "plan_code", nullable = false)
    private String planCode;

    @Column(nullable = false, unique = true)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProvisioningStatus status = ProvisioningStatus.ACTIVE;

    @Column(name = "provisioned_at", nullable = false)
    @Builder.Default
    private LocalDateTime provisionedAt = LocalDateTime.now();

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;
}
