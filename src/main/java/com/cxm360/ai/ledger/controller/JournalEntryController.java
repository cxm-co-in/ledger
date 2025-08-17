package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.dto.CreateJournalEntryRequest;
import com.cxm360.ai.ledger.dto.JournalEntryDto;
import com.cxm360.ai.ledger.service.JournalEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @PostMapping("/ledgers/{ledgerId}/journal-entries")
    public ResponseEntity<JournalEntryDto> createJournalEntry(
            @PathVariable UUID ledgerId,
            @Valid @RequestBody CreateJournalEntryRequest request) {
        JournalEntryDto createdEntry = journalEntryService.createJournalEntry(ledgerId, request);
        return new ResponseEntity<>(createdEntry, HttpStatus.CREATED);
    }

    @GetMapping("/journal-entries/{journalEntryId}")
    public ResponseEntity<JournalEntryDto> getJournalEntryById(@PathVariable UUID journalEntryId) {
        return journalEntryService.getJournalEntryById(journalEntryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/journal-entries/{journalEntryId}/post")
    public ResponseEntity<JournalEntryDto> postJournalEntry(@RequestHeader("X-Tenant-ID") UUID tenantId, @PathVariable UUID journalEntryId) {
        // TODO: Add exception handling for business logic errors (e.g., posting a non-draft entry)
        JournalEntryDto postedEntry = journalEntryService.postJournalEntry(journalEntryId);
        return ResponseEntity.ok(postedEntry);
    }
}
