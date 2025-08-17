package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.*;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.JournalEntryStatus;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Validator for JournalEntry creation and posting operations.
 * This demonstrates how to separate complex validation logic from business logic.
 */
public class JournalEntryValidator {
    
    /**
     * Validates that a journal entry can be created.
     */
    public static BasicValidationResult<JournalEntry> validateCreation(JournalEntry journalEntry, UUID tenantId) {
        return SimpleValidator.of(journalEntry)
                .rule(je -> je != null, "Journal entry cannot be null")
                .rule(je -> je.getLedger() != null, "Ledger cannot be null")
                .rule(je -> je.getLedger().getTenant().getId().equals(tenantId), "Ledger does not belong to current tenant")
                .rule(je -> je.getJournalLines() != null && !je.getJournalLines().isEmpty(), "Journal entry must have at least one line")
                .validate();
    }
    
    /**
     * Validates that a journal entry can be posted.
     */
    public static BasicValidationResult<JournalEntry> validateCanPost(JournalEntry journalEntry, UUID tenantId) {
        return SimpleValidator.of(journalEntry)
                .rule(je -> je != null, "Journal entry cannot be null")
                .rule(je -> je.getTenant().getId().equals(tenantId), "Journal entry does not belong to current tenant")
                .rule(je -> je.getStatus() == JournalEntryStatus.DRAFT, "Only DRAFT entries can be posted")
                .rule(je -> je.getJournalLines() != null && !je.getJournalLines().isEmpty(), "Journal entry must have at least one line")
                .validate();
    }
    
    /**
     * Validates that all journal lines have valid accounts.
     */
    public static BasicValidationResult<List<JournalLine>> validateJournalLines(List<JournalLine> lines, UUID tenantId) {
        if (lines == null || lines.isEmpty()) {
            return BasicValidationResult.failure("Journal entry must have at least one line");
        }
        
        for (JournalLine line : lines) {
            if (line.getAccount() == null) {
                return BasicValidationResult.failure("Journal line must have an account");
            }
            
            if (line.getAccount().getTenant() == null || !line.getAccount().getTenant().getId().equals(tenantId)) {
                return BasicValidationResult.failure("Account does not belong to current tenant");
            }
            
            if (!line.getAccount().isActive()) {
                return BasicValidationResult.failure("Account is not active: " + line.getAccount().getCode());
            }
        }
        
        return BasicValidationResult.success(lines);
    }
    
    /**
     * Validates currency constraints for journal lines.
     */
    public static BasicValidationResult<List<JournalLine>> validateCurrencyConstraints(List<JournalLine> lines) {
        for (JournalLine line : lines) {
            Account account = line.getAccount();
            
            if (account.getCurrencyMode() == CurrencyMode.SINGLE) {
                if (!line.getCurrencyCode().equals(account.getCurrencyCode())) {
                    return BasicValidationResult.failure(
                        "Account " + account.getCode() + " only accepts currency: " + account.getCurrencyCode()
                    );
                }
            }
        }
        
        return BasicValidationResult.success(lines);
    }
    
    /**
     * Validates that the accounting date is in an open period.
     */
    public static BasicValidationResult<Period> validateAccountingDate(Period period, LocalDate accountingDate) {
        if (period == null) {
            return BasicValidationResult.failure("No period found for accounting date: " + accountingDate);
        }
        
        if (period.getStatus() != PeriodStatus.OPEN) {
            return BasicValidationResult.failure("Period is not open for accounting date: " + accountingDate);
        }
        
        return BasicValidationResult.success(period);
    }
    
    /**
     * Validates that a journal entry balances (debits equal credits).
     */
    public static BasicValidationResult<JournalEntry> validateBalancing(JournalEntry journalEntry, long totalDebits, long totalCredits) {
        if (totalDebits != totalCredits) {
            return BasicValidationResult.failure(
                String.format("Journal entry does not balance. Debits: %d, Credits: %d", totalDebits, totalCredits)
            );
        }
        
        return BasicValidationResult.success(journalEntry);
    }
    
    /**
     * Validates that all required fields are present for posting.
     */
    public static BasicValidationResult<JournalEntry> validatePostingRequirements(JournalEntry journalEntry) {
        return SimpleValidator.of(journalEntry)
                .rule(je -> je.getAccountingDate() != null, "Accounting date cannot be null")
                .rule(je -> je.getLedger() != null, "Ledger cannot be null")
                .rule(je -> je.getTenant() != null, "Tenant cannot be null")
                .rule(je -> je.getJournalLines() != null && !je.getJournalLines().isEmpty(), "Journal lines cannot be null or empty")
                .validate();
    }
    
    /**
     * Validates that a journal line has all required fields.
     */
    public static BasicValidationResult<JournalLine> validateJournalLine(JournalLine line) {
        return SimpleValidator.of(line)
                .rule(l -> l != null, "Journal line cannot be null")
                .rule(l -> l.getAccount() != null, "Account cannot be null")
                .rule(l -> l.getDirection() != null, "Direction cannot be null")
                .rule(l -> l.getAmountMinor() > 0, "Amount must be positive")
                .rule(l -> l.getCurrencyCode() != null && !l.getCurrencyCode().trim().isEmpty(), "Currency code cannot be null or empty")
                .validate();
    }
}
