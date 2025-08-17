package com.cxm360.ai.ledger.model.enums;

/**
 * Represents the status of an accounting period.
 */
public enum PeriodStatus {
    /**
     * The period is open and transactions can be posted.
     */
    OPEN,

    /**
     * The period is closed and no more regular transactions can be posted.
     * Only adjusting entries by privileged users may be allowed.
     */
    CLOSED,

    /**
     * The period is permanently locked and no further changes of any kind are permitted.
     */
    LOCKED
}
