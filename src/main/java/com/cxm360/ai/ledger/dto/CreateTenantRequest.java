package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new tenant.
 */
public record CreateTenantRequest(
    
    /**
     * The name of the tenant (e.g., "Acme Corporation", "XYZ Inc").
     */
    @NotBlank(message = "Tenant name is required")
    @Size(min = 1, max = 255, message = "Tenant name must be between 1 and 255 characters")
    String name,
    
    /**
     * JSON object for tenant-specific settings and configuration.
     * Optional field for storing tenant preferences, branding, etc.
     */
    JsonNode settings
) {}
