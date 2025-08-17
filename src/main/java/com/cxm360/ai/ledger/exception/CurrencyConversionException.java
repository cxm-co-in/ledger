package com.cxm360.ai.ledger.exception;

/**
 * Exception thrown when currency conversion operations fail.
 */
public class CurrencyConversionException extends RuntimeException {
    
    public CurrencyConversionException(String message) {
        super(message);
    }
    
    public CurrencyConversionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CurrencyConversionException(String fromCurrency, String toCurrency, String reason) {
        super(String.format("Cannot convert from %s to %s: %s", fromCurrency, toCurrency, reason));
    }
}
