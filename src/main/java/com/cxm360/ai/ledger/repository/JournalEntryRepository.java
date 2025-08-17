package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the JournalEntry entity.
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
}
