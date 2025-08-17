package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.BalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, UUID> {
    
    /**
     * Find the most recent balance snapshot for an account as of a specific date.
     */
    @Query("SELECT bs FROM BalanceSnapshot bs WHERE bs.tenant.id = :tenantId AND bs.ledger.id = :ledgerId " +
           "AND bs.account.id = :accountId AND bs.currencyCode = :currencyCode " +
           "AND bs.asOfDate <= :asOfDate ORDER BY bs.asOfDate DESC")
    List<BalanceSnapshot> findMostRecentSnapshots(@Param("tenantId") UUID tenantId,
                                                 @Param("ledgerId") UUID ledgerId,
                                                 @Param("accountId") UUID accountId,
                                                 @Param("currencyCode") String currencyCode,
                                                 @Param("asOfDate") LocalDate asOfDate);
    
    /**
     * Find balance snapshots for a ledger as of a specific date.
     */
    @Query("SELECT bs FROM BalanceSnapshot bs WHERE bs.tenant.id = :tenantId AND bs.ledger.id = :ledgerId " +
           "AND bs.asOfDate = :asOfDate")
    List<BalanceSnapshot> findByLedgerAndDate(@Param("tenantId") UUID tenantId,
                                             @Param("ledgerId") UUID ledgerId,
                                             @Param("asOfDate") LocalDate asOfDate);
}
