package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.context.TenantContext;
import com.cxm360.ai.ledger.dto.AccountDto;
import com.cxm360.ai.ledger.dto.CreateAccountRequest;
import com.cxm360.ai.ledger.dto.UpdateAccountRequest;
import com.cxm360.ai.ledger.mapper.AccountMapper;
import com.cxm360.ai.ledger.model.Account;
import com.cxm360.ai.ledger.model.Ledger;
import com.cxm360.ai.ledger.repository.AccountRepository;
import com.cxm360.ai.ledger.repository.LedgerRepository;
import com.cxm360.ai.ledger.repository.TenantRepository;
import com.cxm360.ai.ledger.validation.AccountValidator;
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements com.cxm360.ai.ledger.service.AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TenantRepository tenantRepository;
    private final LedgerRepository ledgerRepository;

    @Override
    @Transactional
    public AccountDto createAccount(UUID ledgerId, CreateAccountRequest request) {
        // Get tenant from context using functional approach
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        // Validate that ledgerId exists and tenantId matches context using functional approach
        Ledger ledger = Option.ofOptional(ledgerRepository.findById(ledgerId))
                .getOrElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));
        
        FunctionalUtils.requireTrue(
            ledger.getTenant().getId().equals(currentTenantId),
            () -> new IllegalArgumentException("Ledger does not belong to current tenant")
        );

        Account account = accountMapper.toEntity(request);
        account.setLedger(ledger);
        
        // Set tenant from context using functional approach
        com.cxm360.ai.ledger.model.Tenant tenant = Option.ofOptional(tenantRepository.findById(currentTenantId))
                .getOrElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));
        account.setTenant(tenant);

        // Handle parent account if specified
        if (request.parentAccountId() != null) {
            Account parentAccount = Option.ofOptional(accountRepository.findById(request.parentAccountId()))
                    .getOrElseThrow(() -> new IllegalArgumentException("Parent account not found"));
            account.setParentAccount(parentAccount);
        }

        // Validate the account using the validation framework
        BasicValidationResult<Account> validationResult = 
                AccountValidator.validateCreation(account, currentTenantId, ledgerId);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Account validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        // Validate currency constraints
        BasicValidationResult<Account> currencyValidation = 
                AccountValidator.validateCurrencyConstraints(account);
        
        FunctionalUtils.requireTrue(
            currencyValidation.isSuccess(),
            () -> new IllegalArgumentException(currencyValidation.getErrorsAsList().get(0))
        );

        // Validate parent account relationship
        BasicValidationResult<Account> parentValidation = 
                AccountValidator.validateParentAccount(account, account.getId());
        
        FunctionalUtils.requireTrue(
            parentValidation.isSuccess(),
            () -> new IllegalArgumentException(parentValidation.getErrorsAsList().get(0))
        );

        // Validate account type consistency
        BasicValidationResult<Account> typeValidation = 
                AccountValidator.validateAccountTypeConsistency(account);
        
        FunctionalUtils.requireTrue(
            typeValidation.isSuccess(),
            () -> new IllegalArgumentException(typeValidation.getErrorsAsList().get(0))
        );

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDto> getAccountById(UUID accountId) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );
        
        return accountRepository.findById(accountId)
                .filter(acc -> acc.getTenant().getId().equals(currentTenantId))
                .map(accountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByLedgerId(UUID ledgerId) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );
        
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getTenant().getId().equals(currentTenantId) && acc.getLedger().getId().equals(ledgerId))
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDto updateAccount(UUID accountId, UpdateAccountRequest request) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        Account account = Option.ofOptional(accountRepository.findById(accountId))
                .getOrElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Verify tenant ownership using validation framework
        BasicValidationResult<Account> ownershipValidation = 
                AccountValidator.validateCanUpdate(account, currentTenantId);
        
        FunctionalUtils.requireTrue(
            ownershipValidation.isSuccess(),
            () -> new IllegalArgumentException(ownershipValidation.getErrorsAsList().get(0))
        );

        // Update fields if provided
        if (request.name() != null) {
            account.setName(request.name());
        }
        if (request.type() != null) {
            account.setType(request.type());
        }
        if (request.normalSide() != null) {
            account.setNormalSide(request.normalSide());
        }
        if (request.currencyMode() != null) {
            account.setCurrencyMode(request.currencyMode());
        }
        if (request.currencyCode() != null) {
            account.setCurrencyCode(request.currencyCode());
        }
        if (request.isActive() != null) {
            account.setIsActive(request.isActive().booleanValue());
        }
        if (request.parentAccountId() != null) {
            if (request.parentAccountId().equals(accountId)) {
                throw new IllegalArgumentException("Account cannot be its own parent");
            }
            Account parentAccount = Option.ofOptional(accountRepository.findById(request.parentAccountId()))
                    .getOrElseThrow(() -> new IllegalArgumentException("Parent account not found"));
            account.setParentAccount(parentAccount);
        }

        // Validate the updated account using validation framework
        BasicValidationResult<Account> validationResult = 
                AccountValidator.validateCreation(account, currentTenantId, account.getLedger().getId());
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Account validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        // Validate currency constraints
        BasicValidationResult<Account> currencyValidation = 
                AccountValidator.validateCurrencyConstraints(account);
        
        FunctionalUtils.requireTrue(
            currencyValidation.isSuccess(),
            () -> new IllegalArgumentException(currencyValidation.getErrorsAsList().get(0))
        );

        // Validate parent account relationship
        BasicValidationResult<Account> parentValidation = 
                AccountValidator.validateParentAccount(account, accountId);
        
        FunctionalUtils.requireTrue(
            parentValidation.isSuccess(),
            () -> new IllegalArgumentException(parentValidation.getErrorsAsList().get(0))
        );

        // Validate account type consistency
        BasicValidationResult<Account> typeValidation = 
                AccountValidator.validateAccountTypeConsistency(account);
        
        FunctionalUtils.requireTrue(
            typeValidation.isSuccess(),
            () -> new IllegalArgumentException(typeValidation.getErrorsAsList().get(0))
        );

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }
}
