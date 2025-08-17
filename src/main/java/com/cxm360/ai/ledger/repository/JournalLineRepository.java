package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the JournalLine entity.
 */
@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
}
