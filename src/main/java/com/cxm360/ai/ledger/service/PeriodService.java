package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.model.Period;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodService {
    
    /**
     * Create a new accounting period.
     */
    Period createPeriod(UUID ledgerId, String name, LocalDate startDate, LocalDate endDate, PeriodStatus status);
    
    /**
     * Get periods for a specific ledger.
     */
    List<Period> getPeriodsByLedger(UUID ledgerId);
    
    /**
     * Get periods by status for a specific ledger.
     */
    List<Period> getPeriodsByStatus(UUID ledgerId, PeriodStatus status);
    
    /**
     * Get the open period for a specific ledger.
     */
    Optional<Period> getOpenPeriod(UUID ledgerId);
    
    /**
     * Close a period (set status to CLOSED).
     */
    Period closePeriod(UUID periodId);
    
    /**
     * Lock a period (set status to LOCKED).
     */
    Period lockPeriod(UUID periodId);
    
    /**
     * Reopen a period (set status to OPEN).
     */
    Period reopenPeriod(UUID periodId);
    
    /**
     * Get period by ID.
     */
    Optional<Period> getPeriodById(UUID periodId);
    
    /**
     * Check if a date falls within an open period for a ledger.
     */
    boolean isDateInOpenPeriod(UUID ledgerId, LocalDate date);
}
