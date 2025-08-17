package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new ledger.
 */
public record CreateLedgerRequest(
    
    /**
     * The human-readable name of the ledger (e.g., "USA Operations", "EU Subsidiary").
     */
    @NotBlank(message = "Ledger name is required")
    @Size(min = 1, max = 255, message = "Ledger name must be between 1 and 255 characters")
    String name,
    
    /**
     * The functional currency for this ledger (e.g., "USD", "EUR").
     * All journal entries must balance in this currency.
     */
    @NotBlank(message = "Functional currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a 3-letter ISO code (e.g., USD, EUR)")
    String functionalCurrencyCode,
    
    /**
     * The timezone for this ledger's accounting dates (e.g., "UTC", "America/New_York").
     */
    @NotBlank(message = "Timezone is required")
    @Size(min = 1, max = 100, message = "Timezone must be between 1 and 100 characters")
    String timezone,
    
    /**
     * A JSON object for ledger-specific settings and policies,
     * such as revaluation methods or default rounding accounts.
     */
    JsonNode settings
) {}
