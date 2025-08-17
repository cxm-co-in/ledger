package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the Period entity.
 */
@Repository
public interface PeriodRepository extends JpaRepository<Period, UUID> {
}
