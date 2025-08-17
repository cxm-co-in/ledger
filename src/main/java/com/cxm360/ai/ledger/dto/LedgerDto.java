package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/**
 * DTO for ledger information.
 */
public record LedgerDto(
    
    /**
     * The unique identifier for the ledger.
     */
    UUID id,
    
    /**
     * The tenant this ledger belongs to.
     */
    TenantDto tenant,
    
    /**
     * The human-readable name of the ledger.
     */
    String name,
    
    /**
     * The functional currency for this ledger.
     */
    String functionalCurrencyCode,
    
    /**
     * The timezone for this ledger's accounting dates.
     */
    String timezone,
    
    /**
     * A JSON object for ledger-specific settings and policies.
     */
    JsonNode settings
) {}
