package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.context.TenantContext;
import com.cxm360.ai.ledger.exception.ResourceNotFoundException;
import com.cxm360.ai.ledger.model.Ledger;
import com.cxm360.ai.ledger.model.Period;
import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;
import com.cxm360.ai.ledger.repository.LedgerRepository;
import com.cxm360.ai.ledger.repository.PeriodRepository;
import com.cxm360.ai.ledger.repository.TenantRepository;
import com.cxm360.ai.ledger.service.PeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PeriodServiceImpl implements PeriodService {

    private final PeriodRepository periodRepository;
    private final LedgerRepository ledgerRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public Period createPeriod(UUID ledgerId, String name, LocalDate startDate, LocalDate endDate, PeriodStatus status) {
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // Validate ledger exists and belongs to current tenant
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
        
        if (!ledger.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Ledger does not belong to current tenant");
        }

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Period name cannot be null or empty");
        }

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Check for overlapping periods
        List<Period> existingPeriods = periodRepository.findByTenantIdAndLedgerIdAndStatus(
                currentTenantId, ledgerId, PeriodStatus.OPEN);
        
        for (Period existingPeriod : existingPeriods) {
            if (datesOverlap(startDate, endDate, existingPeriod.getStartDate(), existingPeriod.getEndDate())) {
                String periodName = existingPeriod.getName() != null ? existingPeriod.getName() : "Unnamed Period";
                throw new IllegalArgumentException("Period dates overlap with existing period: " + periodName);
            }
        }

        // Create new period
        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", currentTenantId.toString()));

        Period period = Period.builder()
                .tenant(tenant)
                .ledger(ledger)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .build();

        return periodRepository.save(period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Period> getPeriodsByLedger(UUID ledgerId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return periodRepository.findByTenantIdAndLedgerIdAndStatus(currentTenantId, ledgerId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Period> getPeriodsByStatus(UUID ledgerId, PeriodStatus status) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return periodRepository.findByTenantIdAndLedgerIdAndStatus(currentTenantId, ledgerId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Period> getOpenPeriod(UUID ledgerId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return periodRepository.findOpenPeriod(currentTenantId, ledgerId);
    }

    @Override
    @Transactional
    public Period closePeriod(UUID periodId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Period period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period", periodId.toString()));

        if (!period.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Period does not belong to current tenant");
        }

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Only OPEN periods can be closed");
        }

        period.setStatus(PeriodStatus.CLOSED);
        return periodRepository.save(period);
    }

    @Override
    @Transactional
    public Period lockPeriod(UUID periodId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Period period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period", periodId.toString()));

        if (!period.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Period does not belong to current tenant");
        }

        if (period.getStatus() != PeriodStatus.CLOSED) {
            throw new IllegalStateException("Only CLOSED periods can be locked");
        }

        period.setStatus(PeriodStatus.LOCKED);
        return periodRepository.save(period);
    }

    @Override
    @Transactional
    public Period reopenPeriod(UUID periodId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Period period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period", periodId.toString()));

        if (!period.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Period does not belong to current tenant");
        }

        if (period.getStatus() == PeriodStatus.LOCKED) {
            throw new IllegalStateException("LOCKED periods cannot be reopened");
        }

        // Check if there's already an open period
        Optional<Period> openPeriod = periodRepository.findOpenPeriod(currentTenantId, period.getLedger().getId());
        if (openPeriod.isPresent() && !openPeriod.get().getId().equals(periodId)) {
            throw new IllegalStateException("Cannot reopen period: another period is already open");
        }

        period.setStatus(PeriodStatus.OPEN);
        return periodRepository.save(period);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Period> getPeriodById(UUID periodId) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return periodRepository.findById(periodId)
                .filter(period -> period.getTenant().getId().equals(currentTenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDateInOpenPeriod(UUID ledgerId, LocalDate date) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Optional<Period> period = periodRepository.findPeriodContainingDate(currentTenantId, ledgerId, date);
        return period.isPresent() && period.get().getStatus() == PeriodStatus.OPEN;
    }

    /**
     * Check if two date ranges overlap.
     */
    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
