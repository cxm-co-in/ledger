package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.dto.CreateJournalEntryRequest;
import com.cxm360.ai.ledger.dto.JournalEntryDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Journal Entries.
 */
public interface JournalEntryService {

    /**
     * Creates a new journal entry in a DRAFT state.
     *
     * @param ledgerId The ID of the ledger this entry belongs to.
     * @param request The DTO containing the details for the new journal entry.
     * @return The newly created journal entry as a DTO.
     */
    JournalEntryDto createJournalEntry(UUID ledgerId, CreateJournalEntryRequest request);

    /**
     * Retrieves a journal entry by its unique ID.
     *
     * @param journalEntryId The ID of the journal entry to retrieve.
     * @return An Optional containing the JournalEntryDto if found, or an empty Optional otherwise.
     */
    Optional<JournalEntryDto> getJournalEntryById(UUID journalEntryId);

    /**
     * Posts a journal entry to the ledger, creating the immutable posting records.
     * This is the most critical operation, performing validation and creating the financial record.
     *
     * @param journalEntryId The ID of the DRAFT journal entry to post.
     * @return The posted journal entry, with its status updated to POSTED.
     */
    JournalEntryDto postJournalEntry(UUID journalEntryId);

}
