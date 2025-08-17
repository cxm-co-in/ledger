package com.cxm360.ai.ledger.exception;

/**
 * Exception thrown when journal entry balancing operations fail.
 */
public class JournalEntryBalancingException extends RuntimeException {
    
    public JournalEntryBalancingException(String message) {
        super(message);
    }
    
    public JournalEntryBalancingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JournalEntryBalancingException(long debits, long credits) {
        super(String.format("Journal entry does not balance. Debits: %d, Credits: %d", debits, credits));
    }
}
