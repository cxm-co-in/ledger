package com.cxm360.ai.ledger.model;

import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single line item within a JournalEntry.
 * Each line records a debit or a credit to a specific account.
 */
@Entity
@Table(name = "journal_line")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NormalSide direction;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "fx_rate")
    private BigDecimal fxRate;

    @Column(name = "functional_amount_minor")
    private Long functionalAmountMinor;

    private String memo;

    @Column(columnDefinition = "jsonb")
    private String dimensions;
    
    /**
     * Set the journal entry for this line.
     * This method is needed for bidirectional relationship management.
     */
    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }
}
