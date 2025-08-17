package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.cxm360.ai.ledger.model.enums.PartyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing party.
 */
public record UpdatePartyRequest(
    
    /**
     * The updated name of the party.
     */
    @Size(min = 1, max = 255, message = "Party name must be between 1 and 255 characters")
    String name,
    
    /**
     * The updated type of the party.
     */
    @NotNull(message = "Party type is required")
    PartyType type,
    
    /**
     * The updated external identifier.
     */
    @Size(max = 255, message = "External ID must be 255 characters or less")
    String externalId,
    
    /**
     * JSON object containing updated contact details.
     */
    JsonNode contactDetails
) {}
