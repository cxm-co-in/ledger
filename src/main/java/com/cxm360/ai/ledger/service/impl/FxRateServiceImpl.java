package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.exception.CurrencyConversionException;
import com.cxm360.ai.ledger.model.FxRate;
import com.cxm360.ai.ledger.repository.FxRateRepository;
import com.cxm360.ai.ledger.service.FxRateService;
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import com.cxm360.ai.ledger.validation.FxRateValidator;
import io.vavr.control.Option;
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
        // Validate all input parameters using validation framework
        BasicValidationResult<FxRateValidator.FxRateRequest> validationResult = 
                FxRateValidator.validateUpsertRequest(baseCode, quoteCode, asOf, rate, source);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("FX rate validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );
        
        // Check if rate already exists for this date using functional approach
        Option<FxRate> existingRate = Option.ofOptional(
            fxRateRepository.findByIdBaseCodeAndIdQuoteCodeAndIdAsOf(baseCode, quoteCode, asOf)
        );
        
        return existingRate
                .map(rateToUpdate -> {
                    // Update existing rate
                    rateToUpdate.setRate(rate);
                    rateToUpdate.setSource(source);
                    return fxRateRepository.save(rateToUpdate);
                })
                .getOrElse(() -> {
                    // Create new rate with composite key
                    FxRate.FxRateId id = new FxRate.FxRateId(baseCode, quoteCode, asOf);
                    FxRate newRate = FxRate.builder()
                            .id(id)
                            .rate(rate)
                            .source(source)
                            .build();
                    return fxRateRepository.save(newRate);
                });
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
        // Validate input parameters using validation framework
        BasicValidationResult<BigDecimal> amountValidation = FxRateValidator.validateConversionAmount(amount);
        FunctionalUtils.requireTrue(
            amountValidation.isSuccess(),
            () -> new IllegalArgumentException(amountValidation.getErrorsAsList().get(0))
        );
        
        BasicValidationResult<String> fromCurrencyValidation = FxRateValidator.validateCurrencyCode(fromCurrency);
        FunctionalUtils.requireTrue(
            fromCurrencyValidation.isSuccess(),
            () -> new IllegalArgumentException(fromCurrencyValidation.getErrorsAsList().get(0))
        );
        
        BasicValidationResult<String> toCurrencyValidation = FxRateValidator.validateCurrencyCode(toCurrency);
        FunctionalUtils.requireTrue(
            toCurrencyValidation.isSuccess(),
            () -> new IllegalArgumentException(toCurrencyValidation.getErrorsAsList().get(0))
        );
        
        BasicValidationResult<LocalDate> dateValidation = FxRateValidator.validateExchangeRateDate(asOf);
        FunctionalUtils.requireTrue(
            dateValidation.isSuccess(),
            () -> new IllegalArgumentException(dateValidation.getErrorsAsList().get(0))
        );

        // Early return if currencies are the same
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        // Get the rate from fromCurrency to toCurrency using functional approach
        Option<FxRate> rate = Option.ofOptional(getMostRecentRate(fromCurrency, toCurrency, asOf));
        
        if (rate.isDefined()) {
            return amount.multiply(rate.get().getRate()).setScale(6, RoundingMode.HALF_UP);
        }

        // If direct rate not found, try to find inverse rate using functional approach
        Option<FxRate> inverseRate = Option.ofOptional(getMostRecentRate(toCurrency, fromCurrency, asOf));
        
        if (inverseRate.isDefined()) {
            // Use inverse rate: amount * (1 / inverse_rate)
            return amount.divide(inverseRate.get().getRate(), 6, RoundingMode.HALF_UP);
        }

        throw new CurrencyConversionException(fromCurrency, toCurrency, "No exchange rate found as of " + asOf);
    }
}
