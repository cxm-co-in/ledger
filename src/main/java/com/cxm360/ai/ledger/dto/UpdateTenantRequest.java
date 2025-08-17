package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing tenant.
 */
public record UpdateTenantRequest(
    
    /**
     * The updated name of the tenant.
     */
    @Size(min = 1, max = 255, message = "Tenant name must be between 1 and 255 characters")
    String name,
    
    /**
     * JSON object for tenant-specific settings and configuration.
     */
    JsonNode settings
) {}
