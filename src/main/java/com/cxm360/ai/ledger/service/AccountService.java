package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.dto.AccountDto;
import com.cxm360.ai.ledger.dto.CreateAccountRequest;
import com.cxm360.ai.ledger.dto.UpdateAccountRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Accounts.
 */
public interface AccountService {

    /**
     * Creates a new account within a specific ledger.
     *
     * @param ledgerId the ID of the ledger this account belongs to.
     * @param request the DTO containing the details for the new account.
     * @return the newly created account as a DTO.
     */
    AccountDto createAccount(UUID ledgerId, CreateAccountRequest request);

    /**
     * Retrieves an account by its unique ID.
     *
     * @param accountId the ID of the account to retrieve.
     * @return an Optional containing the AccountDto if found, or an empty Optional otherwise.
     */
    Optional<AccountDto> getAccountById(UUID accountId);

    /**
     * Retrieves all accounts belonging to a specific ledger.
     *
     * @param ledgerId the ID of the ledger.
     * @return a list of accounts as DTOs.
     */
    List<AccountDto> getAccountsByLedgerId(UUID ledgerId);

    /**
     * Updates an existing account.
     *
     * @param accountId the ID of the account to update.
     * @param request the DTO containing the updated details.
     * @return the updated account as a DTO.
     */
    AccountDto updateAccount(UUID accountId, UpdateAccountRequest request);

}
