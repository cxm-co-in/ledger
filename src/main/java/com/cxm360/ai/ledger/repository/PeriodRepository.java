package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Period;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PeriodRepository extends JpaRepository<Period, UUID> {
    
    /**
     * Find the open period for a ledger.
     */
    @Query("SELECT p FROM Period p WHERE p.tenant.id = :tenantId AND p.ledger.id = :ledgerId " +
           "AND p.status = 'OPEN'")
    Optional<Period> findOpenPeriod(@Param("tenantId") UUID tenantId, @Param("ledgerId") UUID ledgerId);
    
    /**
     * Find periods for a ledger by status.
     */
    List<Period> findByTenantIdAndLedgerIdAndStatus(UUID tenantId, UUID ledgerId, PeriodStatus status);
    
    /**
     * Find all periods for a ledger (when status is null).
     */
    List<Period> findByTenantIdAndLedgerId(UUID tenantId, UUID ledgerId);
    
    /**
     * Find the period that contains a specific date.
     */
    @Query("SELECT p FROM Period p WHERE p.tenant.id = :tenantId AND p.ledger.id = :ledgerId " +
           "AND p.startDate <= :date AND p.endDate >= :date")
    Optional<Period> findPeriodContainingDate(@Param("tenantId") UUID tenantId, 
                                             @Param("ledgerId") UUID ledgerId, 
                                             @Param("date") LocalDate date);
}
