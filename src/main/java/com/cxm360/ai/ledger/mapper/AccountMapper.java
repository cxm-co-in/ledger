package com.cxm360.ai.ledger.mapper;

import com.cxm360.ai.ledger.dto.AccountDto;
import com.cxm360.ai.ledger.dto.CreateAccountRequest;
import com.cxm360.ai.ledger.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Don't fail on unmapped fields
)
public interface AccountMapper {

    /**
     * Maps an Account entity to an AccountDto.
     *
     * @param account The source Account entity.
     * @return The mapped AccountDto.
     */
    @Mapping(source = "parentAccount.id", target = "parentAccountId")
    AccountDto toDto(Account account);

    /**
     * Maps a CreateAccountRequest DTO to an Account entity.
     *
     * @param request The source CreateAccountRequest DTO.
     * @return The mapped Account entity.
     */
    @Mapping(target = "id", ignore = true) // Let DB generate the ID
    @Mapping(target = "tenant", ignore = true) // Should be set by the service from context
    @Mapping(target = "ledger", ignore = true) // Should be set by the service from context
    @Mapping(target = "isActive", constant = "true") // Default new accounts to active
    @Mapping(target = "parentAccount", ignore = true) // Handled separately in the service
    Account toEntity(CreateAccountRequest request);
}
