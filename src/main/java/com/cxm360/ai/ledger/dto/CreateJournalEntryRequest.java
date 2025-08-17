package com.cxm360.ai.ledger.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating a new JournalEntry.
 */
public record CreateJournalEntryRequest(
        @NotNull LocalDate accountingDate,
        LocalDate transactionDate,
        String description,
        String externalId,
        String idempotencyKey,
        String metadata,

        @NotNull
        @Size(min = 2) // A journal entry must have at least two lines (a debit and a credit)
        @Valid // This annotation cascades validation to the objects in the list
        List<CreateJournalLineRequest> journalLines
) {
}
