package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.dto.CreatePartyRequest;
import com.cxm360.ai.ledger.dto.PartyDto;
import com.cxm360.ai.ledger.dto.UpdatePartyRequest;
import com.cxm360.ai.ledger.model.enums.PartyType;
import com.cxm360.ai.ledger.service.PartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @PostMapping("/parties")
    public ResponseEntity<PartyDto> createParty(@Valid @RequestBody CreatePartyRequest request) {
        PartyDto createdParty = partyService.createParty(request);
        return new ResponseEntity<>(createdParty, HttpStatus.CREATED);
    }

    @GetMapping("/parties/{partyId}")
    public ResponseEntity<PartyDto> getPartyById(@PathVariable UUID partyId) {
        return partyService.getPartyById(partyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/parties")
    public ResponseEntity<List<PartyDto>> getParties(
            @RequestParam(required = false) PartyType type,
            @RequestParam(required = false) String search) {
        
        if (type != null) {
            List<PartyDto> parties = partyService.getPartiesByType(type);
            return ResponseEntity.ok(parties);
        }
        
        if (search != null && !search.trim().isEmpty()) {
            List<PartyDto> parties = partyService.searchPartiesByName(search.trim());
            return ResponseEntity.ok(parties);
        }
        
        List<PartyDto> parties = partyService.getPartiesForCurrentTenant();
        return ResponseEntity.ok(parties);
    }

    @PutMapping("/parties/{partyId}")
    public ResponseEntity<PartyDto> updateParty(
            @PathVariable UUID partyId,
            @Valid @RequestBody UpdatePartyRequest request) {
        PartyDto updatedParty = partyService.updateParty(partyId, request);
        return ResponseEntity.ok(updatedParty);
    }
}
