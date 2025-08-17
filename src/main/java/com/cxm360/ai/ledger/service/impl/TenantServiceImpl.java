package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.dto.CreateTenantRequest;
import com.cxm360.ai.ledger.dto.TenantDto;
import com.cxm360.ai.ledger.dto.UpdateTenantRequest;
import com.cxm360.ai.ledger.mapper.TenantMapper;
import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.repository.TenantRepository;
import com.cxm360.ai.ledger.service.TenantService;
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import com.cxm360.ai.ledger.validation.TenantValidator;
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
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public TenantDto createTenant(CreateTenantRequest request) {
        Tenant tenant = tenantMapper.toEntity(request);
        
        // Validate the tenant using the validation framework
        BasicValidationResult<Tenant> validationResult = 
                TenantValidator.validateCreation(tenant);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Tenant validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        Tenant savedTenant = tenantRepository.save(tenant);
        return tenantMapper.toDto(savedTenant);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantDto> getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId).map(tenantMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(tenantMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TenantDto updateTenant(UUID tenantId, UpdateTenantRequest request) {
        Tenant tenant = Option.ofOptional(tenantRepository.findById(tenantId))
                .getOrElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // Verify tenant can be updated using validation framework
        BasicValidationResult<Tenant> updateValidation = 
                TenantValidator.validateCanUpdate(tenant);
        
        FunctionalUtils.requireTrue(
            updateValidation.isSuccess(),
            () -> new IllegalArgumentException(updateValidation.getErrorsAsList().get(0))
        );

        // Update fields if provided with validation
        if (request.name() != null) {
            BasicValidationResult<String> nameValidation = TenantValidator.validateTenantName(request.name());
            FunctionalUtils.requireTrue(
                nameValidation.isSuccess(),
                () -> new IllegalArgumentException(nameValidation.getErrorsAsList().get(0))
            );
            tenant.setName(request.name());
        }
        
        if (request.settings() != null) {
            BasicValidationResult<JsonNode> settingsValidation = TenantValidator.validateTenantSettings(request.settings());
            FunctionalUtils.requireTrue(
                settingsValidation.isSuccess(),
                () -> new IllegalArgumentException(settingsValidation.getErrorsAsList().get(0))
            );
            tenant.setSettings(request.settings());
        }

        Tenant savedTenant = tenantRepository.save(tenant);
        return tenantMapper.toDto(savedTenant);
    }
}
