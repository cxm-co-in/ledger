package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the Posting entity.
 */
@Repository
public interface PostingRepository extends JpaRepository<Posting, UUID> {
}
