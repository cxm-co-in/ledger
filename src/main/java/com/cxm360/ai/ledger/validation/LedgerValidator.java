package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.Ledger;
import com.cxm360.ai.ledger.model.Currency;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Validator for Ledger creation and update operations.
 * This demonstrates how to separate validation logic from business logic.
 */
public class LedgerValidator {
    
    /**
     * Validates that a ledger can be created.
     */
    public static BasicValidationResult<Ledger> validateCreation(Ledger ledger, UUID tenantId) {
        return SimpleValidator.of(ledger)
                .rule(l -> l != null, "Ledger cannot be null")
                .rule(l -> l.getTenant() != null, "Tenant cannot be null")
                .rule(l -> l.getTenant().getId().equals(tenantId), "Ledger must belong to current tenant")
                .rule(l -> l.getName() != null && !l.getName().trim().isEmpty(), "Ledger name cannot be null or empty")
                .rule(l -> l.getName().trim().length() <= 255, "Ledger name must be 255 characters or less")
                .rule(l -> l.getFunctionalCurrency() != null, "Functional currency cannot be null")
                .rule(l -> l.getTimezone() != null && !l.getTimezone().trim().isEmpty(), "Timezone cannot be null or empty")
                .validate();
    }
    
    /**
     * Validates that a ledger can be updated.
     */
    public static BasicValidationResult<Ledger> validateCanUpdate(Ledger ledger, UUID tenantId) {
        return SimpleValidator.of(ledger)
                .rule(l -> l != null, "Ledger cannot be null")
                .rule(l -> l.getTenant() != null, "Ledger must have a tenant")
                .rule(l -> l.getTenant().getId().equals(tenantId), "Ledger does not belong to current tenant")
                .validate();
    }
    
    /**
     * Validates ledger name format.
     */
    public static BasicValidationResult<String> validateLedgerName(String name) {
        return SimpleValidator.of(name)
                .rule(n -> n != null && !n.trim().isEmpty(), "Ledger name cannot be null or empty")
                .rule(n -> n.trim().length() <= 255, "Ledger name must be 255 characters or less")
                .rule(n -> n.trim().length() >= 3, "Ledger name must be at least 3 characters")
                .validate();
    }
    
    /**
     * Validates timezone format.
     */
    public static BasicValidationResult<String> validateTimezone(String timezone) {
        return SimpleValidator.of(timezone)
                .rule(t -> t != null && !t.trim().isEmpty(), "Timezone cannot be null or empty")
                .rule(t -> t.matches("^[A-Za-z_]+/[A-Za-z_]+$"), "Timezone must be in format Region/City (e.g., America/New_York)")
                .validate();
    }
    
    /**
     * Validates that a currency exists and is valid.
     */
    public static BasicValidationResult<Currency> validateCurrency(Currency currency) {
        if (currency == null) {
            return BasicValidationResult.failure("Currency cannot be null");
        }
        
        if (currency.getCode() == null || currency.getCode().trim().isEmpty()) {
            return BasicValidationResult.failure("Currency code cannot be null or empty");
        }
        
        if (!currency.getCode().matches("^[A-Z]{3}$")) {
            return BasicValidationResult.failure("Currency code must be a 3-letter ISO code (e.g., USD, EUR)");
        }
        
        if (currency.getName() == null || currency.getName().trim().isEmpty()) {
            return BasicValidationResult.failure("Currency name cannot be null or empty");
        }
        
        return BasicValidationResult.success(currency);
    }
    
    /**
     * Validates ledger settings format.
     */
    public static BasicValidationResult<JsonNode> validateLedgerSettings(JsonNode settings) {
        if (settings == null) {
            return BasicValidationResult.success(settings); // Settings are optional
        }
        
        // Basic JSON validation - in a real implementation, you might want more sophisticated validation
        if (settings.isTextual() && settings.asText().trim().isEmpty()) {
            return BasicValidationResult.failure("Ledger settings cannot be empty string");
        }
        
        if (settings.toString().length() > 10000) { // Reasonable limit for settings
            return BasicValidationResult.failure("Ledger settings too large (max 10KB)");
        }
        
        return BasicValidationResult.success(settings);
    }
    
    /**
     * Validates that a ledger can be deleted.
     */
    public static BasicValidationResult<Ledger> validateCanDelete(Ledger ledger) {
        // Check if ledger has any accounts, periods, or journal entries
        // This would require additional repository calls in a real implementation
        if (ledger != null) {
            return BasicValidationResult.success(ledger);
        } else {
            return BasicValidationResult.failure("Ledger cannot be null");
        }
    }
    
    /**
     * Validates ledger code format (if applicable).
     */
    public static BasicValidationResult<String> validateLedgerCode(String code) {
        if (code == null) {
            return BasicValidationResult.success(code); // Code is optional
        }
        
        return SimpleValidator.of(code)
                .rule(c -> c.trim().length() <= 50, "Ledger code must be 50 characters or less")
                .rule(c -> c.matches("^[A-Z0-9_-]+$"), "Ledger code must contain only uppercase letters, numbers, hyphens, and underscores")
                .validate();
    }
}
