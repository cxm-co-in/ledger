package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.dto.CreateTenantRequest;
import com.cxm360.ai.ledger.dto.TenantDto;
import com.cxm360.ai.ledger.dto.UpdateTenantRequest;
import com.cxm360.ai.ledger.mapper.TenantMapper;
import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.repository.TenantRepository;
import com.cxm360.ai.ledger.service.TenantService;
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
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // Update fields if provided
        if (request.name() != null) {
            tenant.setName(request.name());
        }
        if (request.settings() != null) {
            tenant.setSettings(request.settings());
        }

        Tenant savedTenant = tenantRepository.save(tenant);
        return tenantMapper.toDto(savedTenant);
    }
}
