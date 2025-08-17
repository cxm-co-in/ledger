package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.dto.CreateLedgerRequest;
import com.cxm360.ai.ledger.dto.LedgerDto;
import com.cxm360.ai.ledger.dto.UpdateLedgerRequest;
import com.cxm360.ai.ledger.service.LedgerService;
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
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/ledgers")
    public ResponseEntity<LedgerDto> createLedger(@Valid @RequestBody CreateLedgerRequest request) {
        LedgerDto createdLedger = ledgerService.createLedger(request);
        return new ResponseEntity<>(createdLedger, HttpStatus.CREATED);
    }

    @GetMapping("/ledgers/{ledgerId}")
    public ResponseEntity<LedgerDto> getLedgerById(@PathVariable UUID ledgerId) {
        return ledgerService.getLedgerById(ledgerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ledgers")
    public ResponseEntity<List<LedgerDto>> getLedgersForCurrentTenant() {
        List<LedgerDto> ledgers = ledgerService.getLedgersForCurrentTenant();
        return ResponseEntity.ok(ledgers);
    }

    @PutMapping("/ledgers/{ledgerId}")
    public ResponseEntity<LedgerDto> updateLedger(
            @PathVariable UUID ledgerId,
            @Valid @RequestBody UpdateLedgerRequest request) {
        LedgerDto updatedLedger = ledgerService.updateLedger(ledgerId, request);
        return ResponseEntity.ok(updatedLedger);
    }
}
