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
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // TODO: Add validation: check if ledger exists, check if tenant matches context

        Account account = accountMapper.toEntity(request);
        
        // Set ledger from context
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));
        account.setLedger(ledger);
        
        // Set tenant from context
        com.cxm360.ai.ledger.model.Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));
        account.setTenant(tenant);

        if (request.parentAccountId() != null) {
            // TODO: Add proper error handling for not found
            Account parentAccount = accountRepository.findById(request.parentAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent account not found"));
            account.setParentAccount(parentAccount);
        }

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDto> getAccountById(UUID accountId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        
        return accountRepository.findById(accountId)
                .filter(acc -> acc.getTenant().getId().equals(currentTenantId))
                .map(accountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByLedgerId(UUID ledgerId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getTenant().getId().equals(currentTenantId) && acc.getLedger().getId().equals(ledgerId))
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDto updateAccount(UUID accountId, UpdateAccountRequest request) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Verify tenant ownership
        if (!account.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Account not found or access denied");
        }

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
            Account parentAccount = accountRepository.findById(request.parentAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent account not found"));
            account.setParentAccount(parentAccount);
        }

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }
}
