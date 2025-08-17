package com.cxm360.ai.ledger.model;

import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.JournalEntryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a journal entry, which is a collection of journal lines that must balance.
 * This is the primary record of a financial transaction.
 */
@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_journal_entry_idempotency_key", columnList = "idempotency_key", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id", nullable = false)
    private Ledger ledger;

    @Column(name = "accounting_date", nullable = false)
    private LocalDate accountingDate;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalEntryStatus status;

    @Column(name = "sequence_no")
    private Long sequenceNo;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    private String description;

    @Column(name = "posted_at")
    private OffsetDateTime postedAt;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @OneToMany(
            mappedBy = "journalEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<JournalLine> journalLines = new ArrayList<>();

    // Helper method to maintain bidirectional consistency
    public void addJournalLine(JournalLine journalLine) {
        journalLines.add(journalLine);
        journalLine.setJournalEntry(this);
    }

    public void removeJournalLine(JournalLine journalLine) {
        journalLines.remove(journalLine);
        journalLine.setJournalEntry(null);
    }
    
    /**
     * Set the ledger for this journal entry.
     * This method is needed for service layer operations.
     */
    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }
}
