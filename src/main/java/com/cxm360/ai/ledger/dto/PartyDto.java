package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.cxm360.ai.ledger.model.enums.PartyType;

import java.util.UUID;

/**
 * DTO for party information.
 */
public record PartyDto(
    
    /**
     * The unique identifier for the party.
     */
    UUID id,
    
    /**
     * The tenant this party belongs to.
     */
    TenantDto tenant,
    
    /**
     * The name of the party.
     */
    String name,
    
    /**
     * The type of the party.
     */
    PartyType type,
    
    /**
     * An optional external identifier.
     */
    String externalId,
    
    /**
     * JSON object containing contact details.
     */
    JsonNode contactDetails
) {}
