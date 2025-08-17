package com.cxm360.ai.ledger.dto;

import com.cxm360.ai.ledger.model.enums.NormalSide;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new JournalLine within a JournalEntry.
 */
public record CreateJournalLineRequest(
        @NotNull UUID accountId,
        UUID partyId,
        @NotNull NormalSide direction,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @Min(1) long amountMinor,
        BigDecimal fxRate,
        String memo,
        String dimensions
) {
}
