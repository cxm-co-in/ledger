package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.dto.CreateLedgerRequest;
import com.cxm360.ai.ledger.dto.LedgerDto;
import com.cxm360.ai.ledger.dto.UpdateLedgerRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Ledgers.
 */
public interface LedgerService {

    /**
     * Creates a new ledger for a specific tenant.
     *
     * @param request the DTO containing the details for the new ledger.
     * @return the newly created ledger as a DTO.
     */
    LedgerDto createLedger(CreateLedgerRequest request);

    /**
     * Retrieves a ledger by its unique ID.
     *
     * @param ledgerId the ID of the ledger to retrieve.
     * @return an Optional containing the LedgerDto if found, or an empty Optional otherwise.
     */
    Optional<LedgerDto> getLedgerById(UUID ledgerId);

    /**
     * Retrieves all ledgers for the current tenant.
     *
     * @return a list of ledgers as DTOs.
     */
    List<LedgerDto> getLedgersForCurrentTenant();

    /**
     * Updates an existing ledger.
     *
     * @param ledgerId the ID of the ledger to update.
     * @param request the DTO containing the updated details.
     * @return the updated ledger as a DTO.
     */
    LedgerDto updateLedger(UUID ledgerId, UpdateLedgerRequest request);
}
