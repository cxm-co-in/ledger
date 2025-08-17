package com.cxm360.ai.ledger.model.enums;

/**
 * Represents the financial type of an account, corresponding to the major elements of a balance sheet and income statement.
 */
public enum AccountType {
    /**
     * Economic resources owned by the company (e.g., Cash, Accounts Receivable, Equipment).
     */
    ASSET,

    /**
     * Obligations of the company (e.g., Accounts Payable, Loans).
     */
    LIABILITY,

    /**
     * The residual interest in the assets of the entity after deducting liabilities (e.g., Retained Earnings, Common Stock).
     */
    EQUITY,

    /**
     * Gross inflow of economic benefits during the period arising in the course of the ordinary activities (e.g., Sales Revenue).
     */
    REVENUE,

    /**
     * Costs incurred in the process of earning revenue (e.g., Cost of Goods Sold, Salaries Expense).
     */
    EXPENSE,

    /**
     * A general ledger account which is used to reduce the value of a related account.
     * It has a normal balance that is the opposite of the related account.
     * (e.g., Accumulated Depreciation, Sales Returns).
     */
    CONTRA
}
