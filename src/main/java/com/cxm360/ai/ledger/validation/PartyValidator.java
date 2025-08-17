package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.Party;
import com.cxm360.ai.ledger.model.enums.PartyType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Validator for Party creation and update operations.
 * This demonstrates how to separate validation logic from business logic.
 */
public class PartyValidator {
    
    /**
     * Validates that a party can be created.
     */
    public static BasicValidationResult<Party> validateCreation(Party party, UUID tenantId) {
        return SimpleValidator.of(party)
                .rule(p -> p != null, "Party cannot be null")
                .rule(p -> p.getTenant() != null, "Tenant cannot be null")
                .rule(p -> p.getTenant().getId().equals(tenantId), "Party must belong to current tenant")
                .rule(p -> p.getName() != null && !p.getName().trim().isEmpty(), "Party name cannot be null or empty")
                .rule(p -> p.getName().trim().length() <= 255, "Party name must be 255 characters or less")
                .rule(p -> p.getName().trim().length() >= 2, "Party name must be at least 2 characters")
                .rule(p -> p.getType() != null, "Party type cannot be null")
                .validate();
    }
    
    /**
     * Validates that a party can be updated.
     */
    public static BasicValidationResult<Party> validateCanUpdate(Party party, UUID tenantId) {
        return SimpleValidator.of(party)
                .rule(p -> p != null, "Party cannot be null")
                .rule(p -> p.getTenant() != null, "Party must have a tenant")
                .rule(p -> p.getTenant().getId().equals(tenantId), "Party does not belong to current tenant")
                .validate();
    }
    
    /**
     * Validates party name format.
     */
    public static BasicValidationResult<String> validatePartyName(String name) {
        return SimpleValidator.of(name)
                .rule(n -> n != null && !n.trim().isEmpty(), "Party name cannot be null or empty")
                .rule(n -> n.trim().length() <= 255, "Party name must be 255 characters or less")
                .rule(n -> n.trim().length() >= 2, "Party name must be at least 2 characters")
                .rule(n -> n.matches("^[\\p{L}\\p{N}\\s\\-_\\.]+$"), "Party name contains invalid characters")
                .validate();
    }
    
    /**
     * Validates party type constraints.
     */
    public static BasicValidationResult<PartyType> validatePartyType(PartyType type) {
        if (type == null) {
            return BasicValidationResult.failure("Party type cannot be null");
        }
        
        // Validate that the party type is one of the allowed values
        try {
            PartyType.valueOf(type.name());
            return BasicValidationResult.success(type);
        } catch (IllegalArgumentException e) {
            return BasicValidationResult.failure("Invalid party type: " + type);
        }
    }
    
    /**
     * Validates external ID format.
     */
    public static BasicValidationResult<String> validateExternalId(String externalId) {
        if (externalId == null) {
            return BasicValidationResult.success(externalId); // External ID is optional
        }
        
        return SimpleValidator.of(externalId)
                .rule(eid -> eid.trim().length() <= 100, "External ID must be 100 characters or less")
                .rule(eid -> eid.matches("^[\\p{L}\\p{N}\\-_\\.]+$"), "External ID contains invalid characters")
                .validate();
    }
    
    /**
     * Validates contact details format.
     */
    public static BasicValidationResult<JsonNode> validateContactDetails(JsonNode contactDetails) {
        if (contactDetails == null) {
            return BasicValidationResult.success(contactDetails); // Contact details are optional
        }
        
        // Basic JSON validation - in a real implementation, you might want more sophisticated validation
        if (contactDetails.toString().length() > 1000) { // Reasonable limit for contact details
            return BasicValidationResult.failure("Contact details too large (max 1KB)");
        }
        
        return BasicValidationResult.success(contactDetails);
    }
    
    /**
     * Validates search query format.
     */
    public static BasicValidationResult<String> validateSearchQuery(String query) {
        return SimpleValidator.of(query)
                .rule(q -> q != null && !q.trim().isEmpty(), "Search query cannot be null or empty")
                .rule(q -> q.trim().length() <= 100, "Search query must be 100 characters or less")
                .rule(q -> q.trim().length() >= 1, "Search query must be at least 1 character")
                .validate();
    }
    
    /**
     * Validates that a party can be deleted.
     */
    public static BasicValidationResult<Party> validateCanDelete(Party party) {
        // Check if party has any active journal entries or postings
        // This would require additional repository calls in a real implementation
        if (party != null) {
            return BasicValidationResult.success(party);
        } else {
            return BasicValidationResult.failure("Party cannot be null");
        }
    }
    
    /**
     * Validates party identifier format.
     */
    public static BasicValidationResult<String> validatePartyIdentifier(String identifier) {
        if (identifier == null) {
            return BasicValidationResult.success(identifier); // Identifier is optional
        }
        
        return SimpleValidator.of(identifier)
                .rule(id -> id.trim().length() <= 50, "Party identifier must be 50 characters or less")
                .rule(id -> id.matches("^[\\p{L}\\p{N}\\-_\\.]+$"), "Party identifier contains invalid characters")
                .validate();
    }
}
