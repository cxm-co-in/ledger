package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.Tenant;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Validator for Tenant creation and update operations.
 * This demonstrates how to separate validation logic from business logic.
 */
public class TenantValidator {
    
    /**
     * Validates that a tenant can be created.
     */
    public static BasicValidationResult<Tenant> validateCreation(Tenant tenant) {
        return SimpleValidator.of(tenant)
                .rule(t -> t != null, "Tenant cannot be null")
                .rule(t -> t.getName() != null && !t.getName().trim().isEmpty(), "Tenant name cannot be null or empty")
                .rule(t -> t.getName().trim().length() <= 255, "Tenant name must be 255 characters or less")
                .rule(t -> t.getName().trim().length() >= 2, "Tenant name must be at least 2 characters")
                .rule(t -> t.getName().matches("^[\\p{L}\\p{N}\\s\\-_\\.]+$"), "Tenant name contains invalid characters")
                .validate();
    }
    
    /**
     * Validates that a tenant can be updated.
     */
    public static BasicValidationResult<Tenant> validateCanUpdate(Tenant tenant) {
        return SimpleValidator.of(tenant)
                .rule(t -> t != null, "Tenant cannot be null")
                .rule(t -> t.getId() != null, "Tenant must have an ID")
                .validate();
    }
    
    /**
     * Validates tenant name format.
     */
    public static BasicValidationResult<String> validateTenantName(String name) {
        return SimpleValidator.of(name)
                .rule(n -> n != null && !n.trim().isEmpty(), "Tenant name cannot be null or empty")
                .rule(n -> n.trim().length() <= 255, "Tenant name must be 255 characters or less")
                .rule(n -> n.trim().length() >= 2, "Tenant name must be at least 2 characters")
                .rule(n -> n.matches("^[\\p{L}\\p{N}\\s\\-_\\.]+$"), "Tenant name contains invalid characters")
                .validate();
    }
    
    /**
     * Validates tenant settings format.
     */
    public static BasicValidationResult<JsonNode> validateTenantSettings(JsonNode settings) {
        if (settings == null) {
            return BasicValidationResult.success(settings); // Settings are optional
        }
        
        // Basic JSON validation - in a real implementation, you might want more sophisticated validation
        if (settings.toString().length() > 5000) { // Reasonable limit for tenant settings
            return BasicValidationResult.failure("Tenant settings too large (max 5KB)");
        }
        
        return BasicValidationResult.success(settings);
    }
    
    /**
     * Validates that a tenant can be deleted.
     */
    public static BasicValidationResult<Tenant> validateCanDelete(Tenant tenant) {
        // Check if tenant has any active ledgers, accounts, or journal entries
        // This would require additional repository calls in a real implementation
        if (tenant != null) {
            return BasicValidationResult.success(tenant);
        } else {
            return BasicValidationResult.failure("Tenant cannot be null");
        }
    }
    
    /**
     * Validates tenant identifier format.
     */
    public static BasicValidationResult<String> validateTenantIdentifier(String identifier) {
        if (identifier == null) {
            return BasicValidationResult.success(identifier); // Identifier is optional
        }
        
        return SimpleValidator.of(identifier)
                .rule(id -> id.trim().length() <= 50, "Tenant identifier must be 50 characters or less")
                .rule(id -> id.matches("^[\\p{L}\\p{N}\\-_\\.]+$"), "Tenant identifier contains invalid characters")
                .validate();
    }
    
    /**
     * Validates tenant code format (if applicable).
     */
    public static BasicValidationResult<String> validateTenantCode(String code) {
        if (code == null) {
            return BasicValidationResult.success(code); // Code is optional
        }
        
        return SimpleValidator.of(code)
                .rule(c -> c.trim().length() <= 20, "Tenant code must be 20 characters or less")
                .rule(c -> c.matches("^[A-Z0-9_-]+$"), "Tenant code must contain only uppercase letters, numbers, hyphens, and underscores")
                .validate();
    }
}
