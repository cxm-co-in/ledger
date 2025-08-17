package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the Party entity.
 */
@Repository
public interface PartyRepository extends JpaRepository<Party, UUID> {
}
