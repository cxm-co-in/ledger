package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.context.TenantContext;
import com.cxm360.ai.ledger.dto.CreateLedgerRequest;
import com.cxm360.ai.ledger.dto.LedgerDto;
import com.cxm360.ai.ledger.dto.UpdateLedgerRequest;
import com.cxm360.ai.ledger.mapper.LedgerMapper;
import com.cxm360.ai.ledger.model.Ledger;
import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.repository.LedgerRepository;
import com.cxm360.ai.ledger.repository.TenantRepository;
import com.cxm360.ai.ledger.model.Currency;
import com.cxm360.ai.ledger.repository.CurrencyRepository;
import com.cxm360.ai.ledger.service.LedgerService;
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import com.cxm360.ai.ledger.validation.LedgerValidator;
import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerRepository ledgerRepository;
    private final TenantRepository tenantRepository;
    private final LedgerMapper ledgerMapper;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public LedgerDto createLedger(CreateLedgerRequest request) {
        // Get tenant from context using functional approach
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        // Fetch the tenant using functional approach
        Tenant tenant = Option.ofOptional(tenantRepository.findById(currentTenantId))
                .getOrElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));

        // Create the ledger
        Ledger ledger = ledgerMapper.toEntity(request);
        ledger.setTenant(tenant);
        
        // Set the functional currency using functional approach
        Currency currency = Option.ofOptional(currencyRepository.findById(request.functionalCurrencyCode()))
                .getOrElseThrow(() -> new IllegalArgumentException("Currency not found: " + request.functionalCurrencyCode()));
        ledger.setFunctionalCurrency(currency);

        // Validate the ledger using the validation framework
        BasicValidationResult<Ledger> validationResult = 
                LedgerValidator.validateCreation(ledger, currentTenantId);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Ledger validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        // Validate the currency using validation framework
        BasicValidationResult<Currency> currencyValidation = 
                LedgerValidator.validateCurrency(currency);
        
        FunctionalUtils.requireTrue(
            currencyValidation.isSuccess(),
            () -> new IllegalArgumentException(currencyValidation.getErrorsAsList().get(0))
        );

        Ledger savedLedger = ledgerRepository.save(ledger);
        return ledgerMapper.toDto(savedLedger);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LedgerDto> getLedgerById(UUID ledgerId) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        return ledgerRepository.findById(ledgerId)
                .filter(ledger -> ledger.getTenant().getId().equals(currentTenantId))
                .map(ledgerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerDto> getLedgersForCurrentTenant() {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context set"
        );

        return ledgerRepository.findAll().stream()
                .filter(ledger -> ledger.getTenant().getId().equals(currentTenantId))
                .map(ledgerMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LedgerDto updateLedger(UUID ledgerId, UpdateLedgerRequest request) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        Ledger ledger = Option.ofOptional(ledgerRepository.findById(ledgerId))
                .getOrElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));

        // Verify tenant ownership using validation framework
        BasicValidationResult<Ledger> ownershipValidation = 
                LedgerValidator.validateCanUpdate(ledger, currentTenantId);
        
        FunctionalUtils.requireTrue(
            ownershipValidation.isSuccess(),
            () -> new IllegalArgumentException(ownershipValidation.getErrorsAsList().get(0))
        );

        // Update fields if provided with validation
        if (request.name() != null) {
            BasicValidationResult<String> nameValidation = LedgerValidator.validateLedgerName(request.name());
            FunctionalUtils.requireTrue(
                nameValidation.isSuccess(),
                () -> new IllegalArgumentException(nameValidation.getErrorsAsList().get(0))
            );
            ledger.setName(request.name());
        }
        
        if (request.functionalCurrencyCode() != null) {
            Currency currency = Option.ofOptional(currencyRepository.findById(request.functionalCurrencyCode()))
                    .getOrElseThrow(() -> new IllegalArgumentException("Currency not found: " + request.functionalCurrencyCode()));
            
            BasicValidationResult<Currency> currencyValidation = LedgerValidator.validateCurrency(currency);
            FunctionalUtils.requireTrue(
                currencyValidation.isSuccess(),
                () -> new IllegalArgumentException(currencyValidation.getErrorsAsList().get(0))
            );
            ledger.setFunctionalCurrency(currency);
        }
        
        if (request.timezone() != null) {
            BasicValidationResult<String> timezoneValidation = LedgerValidator.validateTimezone(request.timezone());
            FunctionalUtils.requireTrue(
                timezoneValidation.isSuccess(),
                () -> new IllegalArgumentException(timezoneValidation.getErrorsAsList().get(0))
            );
            ledger.setTimezone(request.timezone());
        }
        
        if (request.settings() != null) {
            BasicValidationResult<JsonNode> settingsValidation = LedgerValidator.validateLedgerSettings(request.settings());
            FunctionalUtils.requireTrue(
                settingsValidation.isSuccess(),
                () -> new IllegalArgumentException(settingsValidation.getErrorsAsList().get(0))
            );
            ledger.setSettings(request.settings());
        }

        Ledger savedLedger = ledgerRepository.save(ledger);
        return ledgerMapper.toDto(savedLedger);
    }
}
