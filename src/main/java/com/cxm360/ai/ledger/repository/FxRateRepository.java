package com.cxm360.ai.ledger.repository;

import com.cxm360.ai.ledger.model.FxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FxRateRepository extends JpaRepository<FxRate, FxRate.FxRateId> {
    
    /**
     * Find the most recent exchange rate for a currency pair as of a specific date.
     */
    @Query("SELECT fr FROM FxRate fr WHERE fr.id.baseCode = :baseCode AND fr.id.quoteCode = :quoteCode " +
           "AND fr.id.asOf <= :asOf ORDER BY fr.id.asOf DESC")
    Optional<FxRate> findMostRecentRate(@Param("baseCode") String baseCode, 
                                       @Param("quoteCode") String quoteCode, 
                                       @Param("asOf") LocalDate asOf);
    
    /**
     * Find an exchange rate for a currency pair on a specific date.
     */
    Optional<FxRate> findByIdBaseCodeAndIdQuoteCodeAndIdAsOf(String baseCode, String quoteCode, LocalDate asOf);
}
