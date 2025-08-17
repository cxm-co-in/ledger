package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.dto.CreateTenantRequest;
import com.cxm360.ai.ledger.dto.TenantDto;
import com.cxm360.ai.ledger.dto.UpdateTenantRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Tenants.
 */
public interface TenantService {

    /**
     * Creates a new tenant.
     *
     * @param request the DTO containing the details for the new tenant.
     * @return the newly created tenant as a DTO.
     */
    TenantDto createTenant(CreateTenantRequest request);

    /**
     * Retrieves a tenant by its unique ID.
     *
     * @param tenantId the ID of the tenant to retrieve.
     * @return an Optional containing the TenantDto if found, or an empty Optional otherwise.
     */
    Optional<TenantDto> getTenantById(UUID tenantId);

    /**
     * Retrieves all tenants.
     *
     * @return a list of all tenants as DTOs.
     */
    List<TenantDto> getAllTenants();

    /**
     * Updates an existing tenant.
     *
     * @param tenantId the ID of the tenant to update.
     * @param request the DTO containing the updated details.
     * @return the updated tenant as a DTO.
     */
    TenantDto updateTenant(UUID tenantId, UpdateTenantRequest request);
}
