package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the Tenant entity.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
