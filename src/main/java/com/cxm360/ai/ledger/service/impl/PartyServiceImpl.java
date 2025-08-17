package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.context.TenantContext;
import com.cxm360.ai.ledger.dto.CreatePartyRequest;
import com.cxm360.ai.ledger.dto.PartyDto;
import com.cxm360.ai.ledger.dto.UpdatePartyRequest;
import com.cxm360.ai.ledger.mapper.PartyMapper;
import com.cxm360.ai.ledger.model.Party;
import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.PartyType;
import com.cxm360.ai.ledger.repository.PartyRepository;
import com.cxm360.ai.ledger.repository.TenantRepository;
import com.cxm360.ai.ledger.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final TenantRepository tenantRepository;
    private final PartyMapper partyMapper;

    @Override
    @Transactional
    public PartyDto createParty(CreatePartyRequest request) {
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // Fetch the tenant
        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));

        // Create the party
        Party party = partyMapper.toEntity(request);
        party.setTenant(tenant);

        Party savedParty = partyRepository.save(party);
        return partyMapper.toDto(savedParty);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartyDto> getPartyById(UUID partyId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return partyRepository.findById(partyId)
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .map(partyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyDto> getPartiesForCurrentTenant() {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return partyRepository.findAll().stream()
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .map(partyMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyDto> getPartiesByType(PartyType type) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return partyRepository.findAll().stream()
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .filter(party -> party.getType() == type)
                .map(partyMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyDto> searchPartiesByName(String nameQuery) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        String lowerCaseQuery = nameQuery.toLowerCase();
        return partyRepository.findAll().stream()
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .filter(party -> party.getName().toLowerCase().contains(lowerCaseQuery))
                .map(partyMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PartyDto updateParty(UUID partyId, UpdatePartyRequest request) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found: " + partyId));

        // Verify tenant ownership
        if (!party.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Party not found or access denied");
        }

        // Update fields if provided
        if (request.name() != null) {
            party.setName(request.name());
        }
        if (request.type() != null) {
            party.setType(request.type());
        }
        if (request.externalId() != null) {
            party.setExternalId(request.externalId());
        }
        if (request.contactDetails() != null) {
            party.setContactDetails(request.contactDetails());
        }

        Party savedParty = partyRepository.save(party);
        return partyMapper.toDto(savedParty);
    }
}
