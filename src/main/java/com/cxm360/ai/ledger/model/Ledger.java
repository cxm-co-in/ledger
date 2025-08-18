package com.cxm360.ai.ledger.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.cxm360.ai.ledger.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a ledger, which is a book of financial accounts for a specific entity or tenant.
 * It defines the functional currency and timezone for all its accounts and entries.
 */
@Entity
@Table(name = "ledger")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {

    /**
     * The unique identifier for the ledger.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The tenant this ledger belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * The human-readable name of the ledger (e.g., "USA Operations", "EU Subsidiary").
     */
    @Column(nullable = false)
    private String name;

    /**
     * The functional currency for this ledger (e.g., "USD", "EUR").
     * All journal entries must balance in this currency.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "functional_currency_code", nullable = false)
    private Currency functionalCurrency;

    /**
     * The timezone for this ledger's accounting dates (e.g., "UTC", "America/New_York").
     */
    @Column(nullable = false)
    private String timezone;

    /**
     * A JSON object for ledger-specific settings and policies,
     * such as revaluation methods or default rounding accounts.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode settings;

}
