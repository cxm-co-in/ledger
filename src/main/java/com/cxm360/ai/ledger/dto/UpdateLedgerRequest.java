package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing ledger.
 */
public record UpdateLedgerRequest(
    
    /**
     * The updated human-readable name of the ledger.
     */
    @Size(min = 1, max = 255, message = "Ledger name must be between 1 and 255 characters")
    String name,
    
    /**
     * The updated functional currency for this ledger.
     */
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a 3-letter ISO code (e.g., USD, EUR)")
    String functionalCurrencyCode,
    
    /**
     * The updated timezone for this ledger's accounting dates.
     */
    @Size(min = 1, max = 100, message = "Timezone must be between 1 and 100 characters")
    String timezone,
    
    /**
     * JSON object for ledger-specific settings and policies.
     */
    JsonNode settings
) {}
