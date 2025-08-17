package com.cxm360.ai.ledger.mapper;

import com.cxm360.ai.ledger.dto.CreateJournalEntryRequest;
import com.cxm360.ai.ledger.dto.CreateJournalLineRequest;
import com.cxm360.ai.ledger.dto.JournalEntryDto;
import com.cxm360.ai.ledger.dto.JournalLineDto;
import com.cxm360.ai.ledger.model.JournalEntry;
import com.cxm360.ai.ledger.model.JournalLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface JournalEntryMapper {

    // --- Response DTO Mappers ---

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "party.id", target = "partyId")
    JournalLineDto toDto(JournalLine journalLine);

    @Mapping(source = "ledger.id", target = "ledgerId")
    JournalEntryDto toDto(JournalEntry journalEntry);


    // --- Request DTO Mappers ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", expression = "java(null)") // Explicitly set tenant to null
    @Mapping(target = "journalEntry", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "functionalAmountMinor", ignore = true)
    JournalLine toEntity(CreateJournalLineRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", expression = "java(null)") // Explicitly set tenant to null
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "sequenceNo", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "journalLines", expression = "java(null)") // Explicitly set journalLines to null
    JournalEntry toEntity(CreateJournalEntryRequest request);

}
