package com.cxm360.ai.ledger.dto;

import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing account.
 */
public record UpdateAccountRequest(
    
    /**
     * The updated human-readable name of the account.
     */
    @Size(min = 1, max = 255, message = "Account name must be between 1 and 255 characters")
    String name,
    
    /**
     * The updated financial type of the account.
     */
    @NotNull(message = "Account type is required")
    AccountType type,
    
    /**
     * The updated normal balance side of the account.
     */
    @NotNull(message = "Normal side is required")
    NormalSide normalSide,
    
    /**
     * The updated currency constraint for the account.
     */
    @NotNull(message = "Currency mode is required")
    CurrencyMode currencyMode,
    
    /**
     * The updated specific currency code if currency_mode is SINGLE.
     */
    String currencyCode,
    
    /**
     * Whether the account is active and can be posted to.
     */
    Boolean isActive,
    
    /**
     * The updated parent account ID for hierarchical COA.
     */
    UUID parentAccountId
) {}
