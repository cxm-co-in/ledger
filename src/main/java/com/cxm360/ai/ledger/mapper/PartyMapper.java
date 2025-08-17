package com.cxm360.ai.ledger.mapper;

import com.cxm360.ai.ledger.dto.CreatePartyRequest;
import com.cxm360.ai.ledger.dto.PartyDto;
import com.cxm360.ai.ledger.model.Party;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Party entities and DTOs.
 */
@Mapper(componentModel = "spring", uses = {TenantMapper.class})
public interface PartyMapper {

    /**
     * Convert a CreatePartyRequest to a Party entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    Party toEntity(CreatePartyRequest request);

    /**
     * Convert a Party entity to a PartyDto.
     */
    PartyDto toDto(Party party);
}
