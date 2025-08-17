package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.model.BalanceSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BalanceSnapshotService {
    
    /**
     * Create or update balance snapshots for all accounts in a ledger as of a specific date.
     */
    List<BalanceSnapshot> createBalanceSnapshots(UUID tenantId, UUID ledgerId, LocalDate asOfDate);
    
    /**
     * Get the most recent balance snapshot for an account as of a specific date.
     */
    BalanceSnapshot getMostRecentSnapshot(UUID tenantId, UUID ledgerId, UUID accountId, String currencyCode, LocalDate asOfDate);
    
    /**
     * Get balance snapshots for a ledger as of a specific date.
     */
    List<BalanceSnapshot> getSnapshotsByLedgerAndDate(UUID tenantId, UUID ledgerId, LocalDate asOfDate);
    
    /**
     * Calculate running balance for an account from a specific date.
     */
    long calculateRunningBalance(UUID tenantId, UUID ledgerId, UUID accountId, String currencyCode, LocalDate fromDate, LocalDate toDate);
}
