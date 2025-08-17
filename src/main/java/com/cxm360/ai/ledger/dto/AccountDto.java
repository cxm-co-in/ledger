package com.cxm360.ai.ledger.dto;

import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.NormalSide;

import java.util.UUID;

/**
 * Represents an Account for API responses.
 *
 * @param id The unique identifier for the account.
 * @param code A unique code for the account (e.g., "1010").
 * @param name The human-readable name of the account.
 * @param type The financial type of the account.
 * @param normalSide The normal balance side of the account.
 * @param currencyMode The currency constraint for the account.
 * @param currencyCode The specific currency code if mode is SINGLE.
 * @param isActive Whether the account is active.
 * @param parentAccountId The ID of the parent account, for hierarchies.
 */
public record AccountDto(
        UUID id,
        String code,
        String name,
        AccountType type,
        NormalSide normalSide,
        CurrencyMode currencyMode,
        String currencyCode,
        boolean isActive,
        UUID parentAccountId
) {
}
