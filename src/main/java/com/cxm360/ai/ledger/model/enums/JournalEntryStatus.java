package com.cxm360.ai.ledger.model.enums;

/**
 * Represents the lifecycle status of a journal entry.
 */
public enum JournalEntryStatus {
    /**
     * The entry is a draft and has not been posted. It can be modified.
     */
    DRAFT,

    /**
     * The entry has been posted to the ledger and is immutable.
     */
    POSTED,

    /**
     * The entry has been formally reversed by a new, opposing journal entry.
     */
    REVERSED,

    /**
     * The entry was cancelled before posting and has no financial impact.
     */
    VOID
}
