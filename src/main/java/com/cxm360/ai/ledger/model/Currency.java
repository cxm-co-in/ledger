package com.cxm360.ai.ledger.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Represents a currency with its properties for rounding and display.
 */
@Entity
@Table(name = "currency")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    @Id
    @Column(name = "code", length = 3)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer exponent;

    @Column(name = "rounding_mode", nullable = false)
    private String roundingMode;

    @Column(name = "cash_rounding_increment", precision = 19, scale = 4)
    private BigDecimal cashRoundingIncrement;

    @Column(name = "is_obsolete", nullable = false)
    private Boolean isObsolete;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
