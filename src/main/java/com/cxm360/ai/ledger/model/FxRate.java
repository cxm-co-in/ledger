package com.cxm360.ai.ledger.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Represents an exchange rate between two currencies as of a specific date.
 */
@Entity
@Table(name = "fx_rate", indexes = {
        @Index(name = "idx_fx_rate_pair_date", columnList = "base_code, quote_code, as_of", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FxRate implements Serializable {

    @EmbeddedId
    private FxRateId id;



    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(length = 100)
    private String source;

    @Column(name = "inserted_at", nullable = false)
    private OffsetDateTime insertedAt;

    @PrePersist
    protected void onCreate() {
        insertedAt = OffsetDateTime.now();
    }
    
    /**
     * Composite primary key for FxRate.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class FxRateId implements Serializable {
        
        @Column(name = "base_code", nullable = false, length = 3)
        private String baseCode;
        
        @Column(name = "quote_code", nullable = false, length = 3)
        private String quoteCode;
        
        @Column(name = "as_of", nullable = false)
        private LocalDate asOf;
    }
}
