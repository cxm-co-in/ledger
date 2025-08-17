package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    
    /**
     * Find a currency by its code.
     */
    Optional<Currency> findByCode(String code);
    
    /**
     * Check if a currency exists by its code.
     */
    boolean existsByCode(String code);
}
