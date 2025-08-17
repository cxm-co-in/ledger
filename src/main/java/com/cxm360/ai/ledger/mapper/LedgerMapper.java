package com.cxm360.ai.ledger.mapper;

import com.cxm360.ai.ledger.dto.CreateLedgerRequest;
import com.cxm360.ai.ledger.dto.LedgerDto;
import com.cxm360.ai.ledger.model.Ledger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Ledger entities and DTOs.
 */
@Mapper(componentModel = "spring", uses = {TenantMapper.class})
public interface LedgerMapper {

    /**
     * Convert a CreateLedgerRequest to a Ledger entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "functionalCurrency", ignore = true)
    Ledger toEntity(CreateLedgerRequest request);

    /**
     * Convert a Ledger entity to a LedgerDto.
     */
    @Mapping(source = "functionalCurrency.code", target = "functionalCurrencyCode")
    LedgerDto toDto(Ledger ledger);
}
