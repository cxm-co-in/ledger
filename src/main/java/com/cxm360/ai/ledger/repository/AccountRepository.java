package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the Account entity.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
}
