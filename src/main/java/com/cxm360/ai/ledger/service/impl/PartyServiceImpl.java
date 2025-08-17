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
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import com.cxm360.ai.ledger.validation.PartyValidator;
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
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final TenantRepository tenantRepository;
    private final PartyMapper partyMapper;

    @Override
    @Transactional
    public PartyDto createParty(CreatePartyRequest request) {
        // Get tenant from context using functional approach
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        // Fetch the tenant using functional approach
        Tenant tenant = Option.ofOptional(tenantRepository.findById(currentTenantId))
                .getOrElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));

        // Create the party
        Party party = partyMapper.toEntity(request);
        party.setTenant(tenant);

        // Validate the party using the validation framework
        BasicValidationResult<Party> validationResult = 
                PartyValidator.validateCreation(party, currentTenantId);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Party validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        Party savedParty = partyRepository.save(party);
        return partyMapper.toDto(savedParty);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartyDto> getPartyById(UUID partyId) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        return partyRepository.findById(partyId)
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .map(partyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyDto> getPartiesForCurrentTenant() {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        return partyRepository.findAll().stream()
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .map(partyMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyDto> getPartiesByType(PartyType type) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        return partyRepository.findAll().stream()
                .filter(party -> party.getTenant().getId().equals(currentTenantId))
                .filter(party -> party.getType() == type)
                .map(partyMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyDto> searchPartiesByName(String nameQuery) {
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        // Validate search query using validation framework
        BasicValidationResult<String> queryValidation = 
                PartyValidator.validateSearchQuery(nameQuery);
        
        FunctionalUtils.requireTrue(
            queryValidation.isSuccess(),
            () -> new IllegalArgumentException(queryValidation.getErrorsAsList().get(0))
        );

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
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        Party party = Option.ofOptional(partyRepository.findById(partyId))
                .getOrElseThrow(() -> new IllegalArgumentException("Party not found: " + partyId));

        // Verify tenant ownership using validation framework
        BasicValidationResult<Party> ownershipValidation = 
                PartyValidator.validateCanUpdate(party, currentTenantId);
        
        FunctionalUtils.requireTrue(
            ownershipValidation.isSuccess(),
            () -> new IllegalArgumentException(ownershipValidation.getErrorsAsList().get(0))
        );

        // Update fields if provided with validation
        if (request.name() != null) {
            BasicValidationResult<String> nameValidation = PartyValidator.validatePartyName(request.name());
            FunctionalUtils.requireTrue(
                nameValidation.isSuccess(),
                () -> new IllegalArgumentException(nameValidation.getErrorsAsList().get(0))
            );
            party.setName(request.name());
        }
        
        if (request.type() != null) {
            BasicValidationResult<PartyType> typeValidation = PartyValidator.validatePartyType(request.type());
            FunctionalUtils.requireTrue(
                typeValidation.isSuccess(),
                () -> new IllegalArgumentException(typeValidation.getErrorsAsList().get(0))
            );
            party.setType(request.type());
        }
        
        if (request.externalId() != null) {
            BasicValidationResult<String> externalIdValidation = PartyValidator.validateExternalId(request.externalId());
            FunctionalUtils.requireTrue(
                externalIdValidation.isSuccess(),
                () -> new IllegalArgumentException(externalIdValidation.getErrorsAsList().get(0))
            );
            party.setExternalId(request.externalId());
        }
        
        if (request.contactDetails() != null) {
            BasicValidationResult<JsonNode> contactDetailsValidation = PartyValidator.validateContactDetails(request.contactDetails());
            FunctionalUtils.requireTrue(
                contactDetailsValidation.isSuccess(),
                () -> new IllegalArgumentException(contactDetailsValidation.getErrorsAsList().get(0))
            );
            party.setContactDetails(request.contactDetails());
        }

        Party savedParty = partyRepository.save(party);
        return partyMapper.toDto(savedParty);
    }
}
