package com.cxm360.ai.ledger.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/**
 * DTO for tenant information.
 */
public record TenantDto(
    
    /**
     * The unique identifier for the tenant.
     */
    UUID id,
    
    /**
     * The name of the tenant.
     */
    String name,
    
    /**
     * JSON object for tenant-specific settings and configuration.
     */
    JsonNode settings
) {}
