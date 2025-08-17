package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.exception.CurrencyConversionException;
import com.cxm360.ai.ledger.model.FxRate;
import com.cxm360.ai.ledger.repository.FxRateRepository;
import com.cxm360.ai.ledger.service.FxRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FxRateServiceImpl implements FxRateService {

    private final FxRateRepository fxRateRepository;

    @Override
    @Transactional
    public FxRate upsertFxRate(String baseCode, String quoteCode, LocalDate asOf, BigDecimal rate, String source) {
        // Check if rate already exists for this date
        Optional<FxRate> existingRate = fxRateRepository.findByIdBaseCodeAndIdQuoteCodeAndIdAsOf(baseCode, quoteCode, asOf);
        
        if (existingRate.isPresent()) {
            // Update existing rate
            FxRate rateToUpdate = existingRate.get();
            rateToUpdate.setRate(rate);
            rateToUpdate.setSource(source);
            return fxRateRepository.save(rateToUpdate);
        } else {
            // Create new rate with composite key
            FxRate.FxRateId id = new FxRate.FxRateId(baseCode, quoteCode, asOf);
            FxRate newRate = FxRate.builder()
                    .id(id)
                    .rate(rate)
                    .source(source)
                    .build();
            return fxRateRepository.save(newRate);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FxRate> getMostRecentRate(String baseCode, String quoteCode, LocalDate asOf) {
        return fxRateRepository.findMostRecentRate(baseCode, quoteCode, asOf);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FxRate> getRate(String baseCode, String quoteCode, LocalDate asOf) {
        return fxRateRepository.findByIdBaseCodeAndIdQuoteCodeAndIdAsOf(baseCode, quoteCode, asOf);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency, LocalDate asOf) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        // Get the rate from fromCurrency to toCurrency
        Optional<FxRate> rate = getMostRecentRate(fromCurrency, toCurrency, asOf);
        
        if (rate.isPresent()) {
            return amount.multiply(rate.get().getRate()).setScale(6, RoundingMode.HALF_UP);
        }

        // If direct rate not found, try to find inverse rate
        Optional<FxRate> inverseRate = getMostRecentRate(toCurrency, fromCurrency, asOf);
        
        if (inverseRate.isPresent()) {
            // Use inverse rate: amount * (1 / inverse_rate)
            return amount.divide(inverseRate.get().getRate(), 6, RoundingMode.HALF_UP);
        }

        throw new CurrencyConversionException(fromCurrency, toCurrency, "No exchange rate found as of " + asOf);
    }
}
