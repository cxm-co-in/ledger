package com.cxm360.ai.ledger.dto;

import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for creating a new Account.
 *
 * @param code A unique code for the account.
 * @param name The human-readable name of the account.
 * @param type The financial type of the account.
 * @param normalSide The normal balance side of the account.
 * @param currencyMode The currency constraint for the account.
 * @param currencyCode The specific currency code if mode is SINGLE (required in that case).
 * @param parentAccountId Optional ID of the parent account for hierarchies.
 */
public record CreateAccountRequest(
        @NotBlank @Size(max = 255) String code,
        @NotBlank @Size(max = 255) String name,
        @NotNull AccountType type,
        @NotNull NormalSide normalSide,
        @NotNull CurrencyMode currencyMode,
        @Size(max = 3) String currencyCode,
        UUID parentAccountId
) {
    // TODO: Add custom validation to ensure currencyCode is present if currencyMode is SINGLE.
}
