package com.cxm360.ai.ledger.model;

import com.cxm360.ai.ledger.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents an immutable, atomic posting to a single account.
 * This is the most granular level of financial data and is designed for fast, efficient reporting and aggregation.
 * A collection of postings is generated when a JournalEntry is posted.
 */
@Entity
@Table(name = "postings", indexes = {
        @Index(name = "idx_posting_account_date", columnList = "tenant_id, ledger_id, account_id, accounting_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Posting {

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
    @JoinColumn(name = "entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private JournalLine journalLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;

    @Column(name = "accounting_date", nullable = false)
    private LocalDate accountingDate;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    /**
     * The amount of the posting in the minor units of the currency.
     * This value is signed: positive for DEBIT, negative for CREDIT.
     */
    @Column(name = "amount_minor_signed", nullable = false)
    private long amountMinorSigned;

    /**
     * The timestamp when the posting was recorded.
     */
    @Column(name = "posted_at", nullable = false)
    private OffsetDateTime postedAt;

}
