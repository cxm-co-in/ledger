package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.dto.CreatePartyRequest;
import com.cxm360.ai.ledger.dto.PartyDto;
import com.cxm360.ai.ledger.dto.UpdatePartyRequest;
import com.cxm360.ai.ledger.model.enums.PartyType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Parties.
 */
public interface PartyService {

    /**
     * Creates a new party for the current tenant.
     *
     * @param request the DTO containing the details for the new party.
     * @return the newly created party as a DTO.
     */
    PartyDto createParty(CreatePartyRequest request);

    /**
     * Retrieves a party by its unique ID.
     *
     * @param partyId the ID of the party to retrieve.
     * @return an Optional containing the PartyDto if found, or an empty Optional otherwise.
     */
    Optional<PartyDto> getPartyById(UUID partyId);

    /**
     * Retrieves all parties for the current tenant.
     *
     * @return a list of all parties as DTOs.
     */
    List<PartyDto> getPartiesForCurrentTenant();

    /**
     * Retrieves parties by type for the current tenant.
     *
     * @param type the type of parties to retrieve.
     * @return a list of parties of the specified type.
     */
    List<PartyDto> getPartiesByType(PartyType type);

    /**
     * Searches parties by name for the current tenant.
     *
     * @param nameQuery the name or partial name to search for.
     * @return a list of parties matching the search query.
     */
    List<PartyDto> searchPartiesByName(String nameQuery);

    /**
     * Updates an existing party.
     *
     * @param partyId the ID of the party to update.
     * @param request the DTO containing the updated details.
     * @return the updated party as a DTO.
     */
    PartyDto updateParty(UUID partyId, UpdatePartyRequest request);
}
