package com.cxm360.ai.ledger.service;

import com.cxm360.ai.ledger.model.FxRate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface FxRateService {
    
    /**
     * Create or update an exchange rate for a currency pair.
     */
    FxRate upsertFxRate(String baseCode, String quoteCode, LocalDate asOf, BigDecimal rate, String source);
    
    /**
     * Get the most recent exchange rate for a currency pair as of a specific date.
     */
    Optional<FxRate> getMostRecentRate(String baseCode, String quoteCode, LocalDate asOf);
    
    /**
     * Get an exchange rate for a currency pair on a specific date.
     */
    Optional<FxRate> getRate(String baseCode, String quoteCode, LocalDate asOf);
    
    /**
     * Convert an amount from one currency to another using the rate as of a specific date.
     */
    BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency, LocalDate asOf);
}
