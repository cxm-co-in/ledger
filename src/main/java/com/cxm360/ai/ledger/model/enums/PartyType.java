package com.cxm360.ai.ledger.model.enums;

/**
 * Represents the type of a party in a transaction.
 */
public enum PartyType {
    /**
     * A customer who purchases goods or services.
     */
    CUSTOMER,

    /**
     * A supplier of goods or services.
     */
    VENDOR,

    /**
     * An employee of the organization.
     */
    EMPLOYEE,

    /**
     * Any other type of transacting party.
     */
    OTHER
}
