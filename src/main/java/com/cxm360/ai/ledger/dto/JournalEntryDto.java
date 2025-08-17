package com.cxm360.ai.ledger.dto;

import com.cxm360.ai.ledger.model.enums.JournalEntryStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a JournalEntry for API responses.
 */
public record JournalEntryDto(
        UUID id,
        UUID ledgerId,
        LocalDate accountingDate,
        LocalDate transactionDate,
        JournalEntryStatus status,
        Long sequenceNo,
        String externalId,
        String description,
        OffsetDateTime postedAt,
        String metadata,
        List<JournalLineDto> journalLines
) {
}
