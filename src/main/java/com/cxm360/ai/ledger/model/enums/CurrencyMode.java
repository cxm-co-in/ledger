package com.cxm360.ai.ledger.model.enums;

/**
 * Defines the currency constraints for an account.
 */
public enum CurrencyMode {
    /**
     * The account can only have transactions in its designated single currency.
     */
    SINGLE,

    /**
     * The account can have transactions in multiple currencies.
     */
    MULTI
}
