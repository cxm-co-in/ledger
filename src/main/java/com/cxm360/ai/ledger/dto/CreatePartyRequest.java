package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.cxm360.ai.ledger.model.enums.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new party.
 */
public record CreatePartyRequest(
    
    /**
     * The name of the party (e.g., "Acme Corp", "John Doe").
     */
    @NotBlank(message = "Party name is required")
    @Size(min = 1, max = 255, message = "Party name must be between 1 and 255 characters")
    String name,
    
    /**
     * The type of the party (CUSTOMER, VENDOR, EMPLOYEE, OTHER).
     */
    @NotNull(message = "Party type is required")
    PartyType type,
    
    /**
     * An optional external identifier for linking to other systems.
     */
    @Size(max = 255, message = "External ID must be 255 characters or less")
    String externalId,
    
    /**
     * JSON object containing contact details like address, phone, email, etc.
     */
    JsonNode contactDetails
) {}
