package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.model.Period;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;
import com.cxm360.ai.ledger.repository.PeriodRepository;
import com.cxm360.ai.ledger.service.PeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class PeriodController {

    private final PeriodService periodService;
    private final PeriodRepository periodRepository;

    /**
     * Create a new accounting period.
     */
    @PostMapping
    public ResponseEntity<Period> createPeriod(
            @RequestParam UUID ledgerId,
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "OPEN") PeriodStatus status) {
        
        Period period = periodService.createPeriod(ledgerId, name, startDate, endDate, status);
        return ResponseEntity.ok(period);
    }

    /**
     * Get periods for a specific ledger.
     */
    @GetMapping("/ledger/{ledgerId}")
    public ResponseEntity<List<Period>> getPeriodsByLedger(@PathVariable UUID ledgerId) {
        List<Period> periods = periodService.getPeriodsByLedger(ledgerId);
        return ResponseEntity.ok(periods);
    }

    /**
     * Get periods by status for a specific ledger.
     */
    @GetMapping("/ledger/{ledgerId}/status/{status}")
    public ResponseEntity<List<Period>> getPeriodsByStatus(
            @PathVariable UUID ledgerId, 
            @PathVariable PeriodStatus status) {
        List<Period> periods = periodService.getPeriodsByStatus(ledgerId, status);
        return ResponseEntity.ok(periods);
    }

    /**
     * Get the open period for a specific ledger.
     */
    @GetMapping("/ledger/{ledgerId}/open")
    public ResponseEntity<Period> getOpenPeriod(@PathVariable UUID ledgerId) {
        return periodService.getOpenPeriod(ledgerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Close a period (set status to CLOSED).
     */
    @PostMapping("/{periodId}/close")
    public ResponseEntity<Period> closePeriod(@PathVariable UUID periodId) {
        Period closedPeriod = periodService.closePeriod(periodId);
        return ResponseEntity.ok(closedPeriod);
    }

    /**
     * Lock a period (set status to LOCKED).
     */
    @PostMapping("/{periodId}/lock")
    public ResponseEntity<Period> lockPeriod(@PathVariable UUID periodId) {
        Period lockedPeriod = periodService.lockPeriod(periodId);
        return ResponseEntity.ok(lockedPeriod);
    }

    /**
     * Reopen a period (set status to OPEN).
     */
    @PostMapping("/{periodId}/reopen")
    public ResponseEntity<Period> reopenPeriod(@PathVariable UUID periodId) {
        Period reopenedPeriod = periodService.reopenPeriod(periodId);
        return ResponseEntity.ok(reopenedPeriod);
    }

    /**
     * Get period by ID.
     */
    @GetMapping("/{periodId}")
    public ResponseEntity<Period> getPeriodById(@PathVariable UUID periodId) {
        return periodService.getPeriodById(periodId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
