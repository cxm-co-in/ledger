package com.cxm360.ai.ledger.mapper;

import com.cxm360.ai.ledger.dto.CreateTenantRequest;
import com.cxm360.ai.ledger.dto.TenantDto;
import com.cxm360.ai.ledger.model.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Tenant entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface TenantMapper {

    /**
     * Convert a CreateTenantRequest to a Tenant entity.
     */
    @Mapping(target = "id", ignore = true)
    Tenant toEntity(CreateTenantRequest request);

    /**
     * Convert a Tenant entity to a TenantDto.
     */
    TenantDto toDto(Tenant tenant);
}
