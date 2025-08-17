package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.Period;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Validator for Period creation and updates.
 * This demonstrates how to separate validation logic from business logic.
 */
public class PeriodValidator {
    
    /**
     * Validates a period creation request.
     */
    public static BasicValidationResult<PeriodCreationRequest> validateCreation(
            UUID ledgerId, String name, LocalDate startDate, LocalDate endDate, PeriodStatus status) {
        
        return SimpleValidator.of(new PeriodCreationRequest(ledgerId, name, startDate, endDate, status))
                .rule(req -> req.ledgerId() != null, "Ledger ID cannot be null")
                .rule(req -> req.name() != null && !req.name().trim().isEmpty(), "Period name cannot be null or empty")
                .rule(req -> req.startDate() != null, "Start date cannot be null")
                .rule(req -> req.endDate() != null, "End date cannot be null")
                .rule(req -> req.status() != null, "Status cannot be null")
                .rule(req -> !req.startDate().isAfter(req.endDate()), "Start date cannot be after end date")
                .rule(req -> req.name().trim().length() <= 255, "Period name must be 255 characters or less")
                .validate();
    }
    
    /**
     * Validates that a period can be closed.
     */
    public static BasicValidationResult<Period> validateCanClose(Period period) {
        return SimpleValidator.of(period)
                .rule(p -> p != null, "Period cannot be null")
                .rule(p -> p.getStatus() == PeriodStatus.OPEN, "Only OPEN periods can be closed")
                .validate();
    }
    
    /**
     * Validates that a period can be locked.
     */
    public static BasicValidationResult<Period> validateCanLock(Period period) {
        return SimpleValidator.of(period)
                .rule(p -> p != null, "Period cannot be null")
                .rule(p -> p.getStatus() == PeriodStatus.CLOSED, "Only CLOSED periods can be locked")
                .validate();
    }
    
    /**
     * Validates that a period can be reopened.
     */
    public static BasicValidationResult<Period> validateCanReopen(Period period) {
        return SimpleValidator.of(period)
                .rule(p -> p != null, "Period cannot be null")
                .rule(p -> p.getStatus() != PeriodStatus.LOCKED, "LOCKED periods cannot be reopened")
                .validate();
    }
    
    /**
     * Validates that there are no overlapping periods.
     */
    public static BasicValidationResult<List<Period>> validateNoOverlap(
            LocalDate startDate, LocalDate endDate, List<Period> existingPeriods) {
        
        for (Period existingPeriod : existingPeriods) {
            if (datesOverlap(startDate, endDate, existingPeriod.getStartDate(), existingPeriod.getEndDate())) {
                String periodName = existingPeriod.getName() != null ? existingPeriod.getName() : "Unnamed Period";
                return BasicValidationResult.failure("Period dates overlap with existing period: " + periodName);
            }
        }
        
        return BasicValidationResult.success(existingPeriods);
    }
    
    /**
     * Check if two date ranges overlap.
     */
    private static boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
    
    /**
     * Record representing a period creation request for validation.
     */
    public record PeriodCreationRequest(
            UUID ledgerId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            PeriodStatus status
    ) {}
}
