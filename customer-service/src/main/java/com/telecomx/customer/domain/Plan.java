package com.telecomx.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "data_limit_gb", nullable = false)
    private Integer dataLimitGb;

    @Column(name = "voice_minutes", nullable = false)
    private Integer voiceMinutes;

    @Column(name = "sms_count", nullable = false)
    private Integer smsCount;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
