package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for the JournalEntry entity.
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    
    /**
     * Find the next available sequence number for a ledger.
     */
    @Query("SELECT COALESCE(MAX(je.sequenceNo), 0) + 1 FROM JournalEntry je " +
           "WHERE je.tenant.id = :tenantId AND je.ledger.id = :ledgerId")
    Optional<Long> findNextSequenceNumber(@Param("tenantId") UUID tenantId, @Param("ledgerId") UUID ledgerId);
    
    /**
     * Find journal entries by ledger and status.
     */
    List<JournalEntry> findByTenantIdAndLedgerIdAndStatus(UUID tenantId, UUID ledgerId, String status);
    
    /**
     * Find journal entry by idempotency key for a specific ledger.
     */
    Optional<JournalEntry> findByLedgerIdAndIdempotencyKey(UUID ledgerId, String idempotencyKey);
}
