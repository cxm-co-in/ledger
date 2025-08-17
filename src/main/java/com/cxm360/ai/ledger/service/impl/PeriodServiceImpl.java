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
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import com.cxm360.ai.ledger.validation.PeriodValidator;
import io.vavr.control.Option;
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
        // Get tenant from context using functional approach
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        // Validate ledger exists and belongs to current tenant using functional approach
        Ledger ledger = Option.ofOptional(ledgerRepository.findById(ledgerId))
                .getOrElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
        
        FunctionalUtils.requireTrue(
            ledger.getTenant().getId().equals(currentTenantId),
            () -> new IllegalArgumentException("Ledger does not belong to current tenant")
        );

        // Use validation framework for business rules
        BasicValidationResult<PeriodValidator.PeriodCreationRequest> validationResult = 
                PeriodValidator.validateCreation(ledgerId, name, startDate, endDate, status);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        // Check for overlapping periods using validation framework
        List<Period> existingPeriods = periodRepository.findByTenantIdAndLedgerIdAndStatus(
                currentTenantId, ledgerId, PeriodStatus.OPEN);
        
        BasicValidationResult<List<Period>> overlapValidation = 
                PeriodValidator.validateNoOverlap(startDate, endDate, existingPeriods);
        
        FunctionalUtils.requireTrue(
            overlapValidation.isSuccess(),
            () -> new IllegalArgumentException(overlapValidation.getErrorsAsList().get(0))
        );

        // Create new period using functional approach
        Tenant tenant = Option.ofOptional(tenantRepository.findById(currentTenantId))
                .getOrElseThrow(() -> new ResourceNotFoundException("Tenant", currentTenantId.toString()));

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
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        Period period = Option.ofOptional(periodRepository.findById(periodId))
                .getOrElseThrow(() -> new ResourceNotFoundException("Period", periodId.toString()));

        FunctionalUtils.requireTrue(
            period.getTenant().getId().equals(currentTenantId),
            () -> new IllegalArgumentException("Period does not belong to current tenant")
        );

        // Use validation framework
        BasicValidationResult<Period> validationResult = PeriodValidator.validateCanClose(period);
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalStateException(validationResult.getErrorsAsList().get(0))
        );

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

        // Use validation framework
        BasicValidationResult<Period> validationResult = PeriodValidator.validateCanLock(period);
        if (validationResult.isFailure()) {
            throw new IllegalStateException(validationResult.getErrorsAsList().get(0));
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

        // Use validation framework
        BasicValidationResult<Period> validationResult = PeriodValidator.validateCanReopen(period);
        if (validationResult.isFailure()) {
            throw new IllegalStateException(validationResult.getErrorsAsList().get(0));
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


}
