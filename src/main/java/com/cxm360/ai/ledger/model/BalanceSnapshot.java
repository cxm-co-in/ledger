package com.cxm360.ai.ledger.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a pre-aggregated balance snapshot for an account in a specific currency as of a date.
 * This is designed for fast read performance and reporting.
 */
@Entity
@Table(name = "balance_snapshot", indexes = {
        @Index(name = "idx_balance_snapshot_account_date", 
               columnList = "tenant_id, ledger_id, account_id, currency_code, as_of_date", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id", nullable = false)
    private Ledger ledger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "debit_minor", nullable = false)
    private Long debitMinor;

    @Column(name = "credit_minor", nullable = false)
    private Long creditMinor;

    @Column(name = "balance_minor", nullable = false)
    private Long balanceMinor;

    @Column(name = "entry_count", nullable = false)
    private Integer entryCount;

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
