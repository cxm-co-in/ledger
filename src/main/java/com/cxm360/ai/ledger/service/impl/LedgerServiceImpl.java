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
import com.cxm360.ai.ledger.service.LedgerService;
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

    @Override
    @Transactional
    public LedgerDto createLedger(CreateLedgerRequest request) {
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // Fetch the tenant
        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));

        // Create the ledger
        Ledger ledger = ledgerMapper.toEntity(request);
        ledger.setTenant(tenant);

        Ledger savedLedger = ledgerRepository.save(ledger);
        return ledgerMapper.toDto(savedLedger);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LedgerDto> getLedgerById(UUID ledgerId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return ledgerRepository.findById(ledgerId)
                .filter(ledger -> ledger.getTenant().getId().equals(currentTenantId))
                .map(ledgerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerDto> getLedgersForCurrentTenant() {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return ledgerRepository.findAll().stream()
                .filter(ledger -> ledger.getTenant().getId().equals(currentTenantId))
                .map(ledgerMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LedgerDto updateLedger(UUID ledgerId, UpdateLedgerRequest request) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));

        // Verify tenant ownership
        if (!ledger.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Ledger not found or access denied");
        }

        // Update fields if provided
        if (request.name() != null) {
            ledger.setName(request.name());
        }
        if (request.functionalCurrencyCode() != null) {
            ledger.setFunctionalCurrencyCode(request.functionalCurrencyCode());
        }
        if (request.timezone() != null) {
            ledger.setTimezone(request.timezone());
        }
        if (request.settings() != null) {
            ledger.setSettings(request.settings());
        }

        Ledger savedLedger = ledgerRepository.save(ledger);
        return ledgerMapper.toDto(savedLedger);
    }
}
