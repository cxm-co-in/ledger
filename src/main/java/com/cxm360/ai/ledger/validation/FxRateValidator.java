package com.cxm360.ai.ledger.validation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validator for FxRate operations.
 * This demonstrates how to separate validation logic from business logic.
 */
public class FxRateValidator {
    
    /**
     * Validates currency code format.
     */
    public static BasicValidationResult<String> validateCurrencyCode(String currencyCode) {
        return SimpleValidator.of(currencyCode)
                .rule(cc -> cc != null && !cc.trim().isEmpty(), "Currency code cannot be null or empty")
                .rule(cc -> cc.matches("^[A-Z]{3}$"), "Currency code must be a 3-letter ISO code (e.g., USD, EUR)")
                .validate();
    }
    
    /**
     * Validates that two currency codes are different.
     */
    public static BasicValidationResult<String[]> validateDifferentCurrencies(String baseCode, String quoteCode) {
        if (baseCode == null || quoteCode == null) {
            return BasicValidationResult.failure("Both base and quote currency codes must be provided");
        }
        
        if (baseCode.equals(quoteCode)) {
            return BasicValidationResult.failure("Base and quote currency codes must be different");
        }
        
        return BasicValidationResult.success(new String[]{baseCode, quoteCode});
    }
    
    /**
     * Validates exchange rate value.
     */
    public static BasicValidationResult<BigDecimal> validateExchangeRate(BigDecimal rate) {
        if (rate == null) {
            return BasicValidationResult.failure("Exchange rate cannot be null");
        }
        
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BasicValidationResult.failure("Exchange rate must be positive");
        }
        
        if (rate.compareTo(new BigDecimal("1000000")) > 0) {
            return BasicValidationResult.failure("Exchange rate too large (max 1,000,000)");
        }
        
        return BasicValidationResult.success(rate);
    }
    
    /**
     * Validates date for exchange rate.
     */
    public static BasicValidationResult<LocalDate> validateExchangeRateDate(LocalDate date) {
        if (date == null) {
            return BasicValidationResult.failure("Exchange rate date cannot be null");
        }
        
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            return BasicValidationResult.failure("Exchange rate date cannot be in the future");
        }
        
        // Allow rates from up to 10 years ago
        LocalDate minDate = today.minusYears(10);
        if (date.isBefore(minDate)) {
            return BasicValidationResult.failure("Exchange rate date too old (max 10 years)");
        }
        
        return BasicValidationResult.success(date);
    }
    
    /**
     * Validates source information for exchange rate.
     */
    public static BasicValidationResult<String> validateExchangeRateSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return BasicValidationResult.failure("Exchange rate source cannot be null or empty");
        }
        
        if (source.trim().length() > 100) {
            return BasicValidationResult.failure("Exchange rate source too long (max 100 characters)");
        }
        
        return BasicValidationResult.success(source);
    }
    
    /**
     * Validates amount for currency conversion.
     */
    public static BasicValidationResult<BigDecimal> validateConversionAmount(BigDecimal amount) {
        if (amount == null) {
            return BasicValidationResult.failure("Conversion amount cannot be null");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return BasicValidationResult.failure("Conversion amount cannot be zero");
        }
        
        if (amount.abs().compareTo(new BigDecimal("1000000000000")) > 0) {
            return BasicValidationResult.failure("Conversion amount too large (max 1 trillion)");
        }
        
        return BasicValidationResult.success(amount);
    }
    
    /**
     * Validates all parameters for upserting an exchange rate.
     */
    public static BasicValidationResult<FxRateRequest> validateUpsertRequest(String baseCode, String quoteCode, LocalDate asOf, BigDecimal rate, String source) {
        // Validate individual components
        BasicValidationResult<String> baseCodeValidation = validateCurrencyCode(baseCode);
        if (baseCodeValidation.isFailure()) {
            return BasicValidationResult.failure(baseCodeValidation.getErrorsAsList().get(0));
        }
        
        BasicValidationResult<String> quoteCodeValidation = validateCurrencyCode(quoteCode);
        if (quoteCodeValidation.isFailure()) {
            return BasicValidationResult.failure(quoteCodeValidation.getErrorsAsList().get(0));
        }
        
        BasicValidationResult<String[]> differentCurrenciesValidation = validateDifferentCurrencies(baseCode, quoteCode);
        if (differentCurrenciesValidation.isFailure()) {
            return BasicValidationResult.failure(differentCurrenciesValidation.getErrorsAsList().get(0));
        }
        
        BasicValidationResult<LocalDate> dateValidation = validateExchangeRateDate(asOf);
        if (dateValidation.isFailure()) {
            return BasicValidationResult.failure(dateValidation.getErrorsAsList().get(0));
        }
        
        BasicValidationResult<BigDecimal> rateValidation = validateExchangeRate(rate);
        if (rateValidation.isFailure()) {
            return BasicValidationResult.failure(rateValidation.getErrorsAsList().get(0));
        }
        
        BasicValidationResult<String> sourceValidation = validateExchangeRateSource(source);
        if (sourceValidation.isFailure()) {
            return BasicValidationResult.failure(sourceValidation.getErrorsAsList().get(0));
        }
        
        // All validations passed
        FxRateRequest request = new FxRateRequest(baseCode, quoteCode, asOf, rate, source);
        return BasicValidationResult.success(request);
    }
    
    /**
     * Simple record to hold validated exchange rate request data.
     */
    public record FxRateRequest(String baseCode, String quoteCode, LocalDate asOf, BigDecimal rate, String source) {}
}
