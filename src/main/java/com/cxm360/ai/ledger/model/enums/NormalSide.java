package com.cxm360.ai.ledger.model.enums;

/**
 * Represents the normal balance side of an account.
 * In double-entry bookkeeping, this is the side where increases in the account are recorded.
 */
public enum NormalSide {
    /**
     * The left side of an accounting entry.
     * Assets and Expenses have a normal debit balance.
     */
    DEBIT,

    /**
     * The right side of an accounting entry.
     * Liabilities, Equity, and Revenue have a normal credit balance.
     */
    CREDIT
}
