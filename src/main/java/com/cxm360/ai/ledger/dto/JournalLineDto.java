package com.cxm360.ai.ledger.dto;

import com.cxm360.ai.ledger.model.enums.NormalSide;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a JournalLine for API responses.
 */
public record JournalLineDto(
        UUID id,
        UUID accountId,
        UUID partyId,
        NormalSide direction,
        String currencyCode,
        long amountMinor,
        BigDecimal fxRate,
        Long functionalAmountMinor,
        String memo,
        String dimensions
) {
}
