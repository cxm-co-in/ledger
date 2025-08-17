package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.*;
import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.JournalEntryStatus;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cross-entity validator that demonstrates sophisticated business rule validation.
 * This validator checks relationships and constraints between different entities
 * using Vavr's Validation for accumulating multiple errors.
 */
public class CrossEntityValidator {
    
    /**
     * Validates that a journal entry can be posted considering all related entities.
     * This is a complex validation that checks multiple business rules.
     */
    public static Validation<Seq<String>, JournalEntry> validateJournalEntryPosting(
            JournalEntry journalEntry,
            UUID tenantId,
            List<Account> accounts,
            List<Period> periods,
            List<FxRate> fxRates) {
        
        // Start with basic validation
        BasicValidationResult<JournalEntry> basicValidation = 
                JournalEntryValidator.validateCanPost(journalEntry, tenantId);
        
        if (basicValidation.isFailure()) {
            return Validation.invalid(List.of(basicValidation.getErrorsAsList().get(0)));
        }
        
        // Build comprehensive validation using Vavr's Validation
        return Validation.<Seq<String>, JournalEntry>valid(journalEntry)
                .flatMap(entry -> validateJournalEntryAccounts(entry, accounts, tenantId))
                .flatMap(entry -> validateJournalEntryPeriod(entry, periods, tenantId))
                .flatMap(entry -> validateJournalEntryBalancing(entry, fxRates))
                .flatMap(entry -> validateJournalEntryCurrencyConstraints(entry, accounts))
                .flatMap(entry -> validateJournalEntryBusinessRules(entry, accounts, periods));
    }
    
    /**
     * Validates that all accounts in a journal entry are valid and accessible.
     */
    private static Validation<Seq<String>, JournalEntry> validateJournalEntryAccounts(
            JournalEntry entry,
            List<Account> accounts,
            UUID tenantId) {
        
        List<String> errors = List.empty();
        
        for (JournalLine line : entry.getJournalLines()) {
            Account account = line.getAccount();
            
            // Check if account exists and belongs to tenant
            if (!accounts.exists(acc -> acc.getId().equals(account.getId()))) {
                errors = errors.append("Account not found: " + account.getCode());
                continue;
            }
            
            Account foundAccount = accounts.find(acc -> acc.getId().equals(account.getId())).get();
            
            if (!foundAccount.getTenant().getId().equals(tenantId)) {
                errors = errors.append("Account does not belong to current tenant: " + foundAccount.getCode());
            }
            
            if (!foundAccount.isActive()) {
                errors = errors.append("Account is not active: " + foundAccount.getCode());
            }
        }
        
        return errors.isEmpty() 
                ? Validation.valid(entry) 
                : Validation.invalid(errors);
    }
    
    /**
     * Validates that the journal entry's accounting date falls within an open period.
     */
    private static Validation<Seq<String>, JournalEntry> validateJournalEntryPeriod(
            JournalEntry entry,
            List<Period> periods,
            UUID tenantId) {
        
        LocalDate accountingDate = entry.getAccountingDate();
        
        Option<Period> relevantPeriod = periods
                .find(p -> p.getTenant().getId().equals(tenantId) &&
                           p.getLedger().getId().equals(entry.getLedger().getId()) &&
                           !accountingDate.isBefore(p.getStartDate()) &&
                           !accountingDate.isAfter(p.getEndDate()));
        
        if (relevantPeriod.isEmpty()) {
            return Validation.invalid(List.of("No period found for accounting date: " + accountingDate));
        }
        
        Period period = relevantPeriod.get();
        if (period.getStatus() != PeriodStatus.OPEN) {
            return Validation.invalid(List.of("Period is not open for accounting date: " + accountingDate));
        }
        
        return Validation.valid(entry);
    }
    
    /**
     * Validates that the journal entry balances in functional currency.
     */
    private static Validation<Seq<String>, JournalEntry> validateJournalEntryBalancing(
            JournalEntry entry,
            List<FxRate> fxRates) {
        
        String functionalCurrency = entry.getLedger().getFunctionalCurrency().getCode();
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        
        for (JournalLine line : entry.getJournalLines()) {
            BigDecimal amount = BigDecimal.valueOf(line.getAmountMinor());
            BigDecimal functionalAmount;
            
            if (line.getCurrencyCode().equals(functionalCurrency)) {
                functionalAmount = amount;
            } else {
                // Find FX rate for conversion
                Option<FxRate> rate = fxRates
                        .find(fr -> fr.getId().getBaseCode().equals(line.getCurrencyCode()) &&
                                   fr.getId().getQuoteCode().equals(functionalCurrency) &&
                                   !fr.getId().getAsOf().isAfter(entry.getAccountingDate()));
                
                if (rate.isEmpty()) {
                    return Validation.invalid(List.of("No FX rate found for " + line.getCurrencyCode() + 
                                                   " to " + functionalCurrency + " as of " + entry.getAccountingDate()));
                }
                
                functionalAmount = amount.multiply(rate.get().getRate());
            }
            
            if (line.getDirection() == NormalSide.DEBIT) {
                totalDebits = totalDebits.add(functionalAmount);
            } else {
                totalCredits = totalCredits.add(functionalAmount);
            }
        }
        
        if (totalDebits.compareTo(totalCredits) != 0) {
            return Validation.invalid(List.of(String.format("Journal entry does not balance. Debits: %s, Credits: %s", 
                                                         totalDebits, totalCredits)));
        }
        
        return Validation.valid(entry);
    }
    
    /**
     * Validates currency constraints across journal lines and accounts.
     */
    private static Validation<Seq<String>, JournalEntry> validateJournalEntryCurrencyConstraints(
            JournalEntry entry,
            List<Account> accounts) {
        
        List<String> errors = List.empty();
        
        for (JournalLine line : entry.getJournalLines()) {
            Account account = line.getAccount();
            
            if (account.getCurrencyMode() == CurrencyMode.SINGLE) {
                if (!line.getCurrencyCode().equals(account.getCurrencyCode())) {
                    errors = errors.append(String.format("Account %s only accepts currency: %s", 
                                                       account.getCode(), account.getCurrencyCode()));
                }
            }
        }
        
        return errors.isEmpty() 
                ? Validation.valid(entry) 
                : Validation.invalid(errors);
    }
    
    /**
     * Validates business rules that span multiple entities.
     */
    private static Validation<Seq<String>, JournalEntry> validateJournalEntryBusinessRules(
            JournalEntry entry,
            List<Account> accounts,
            List<Period> periods) {
        
        List<String> errors = List.empty();
        
        // Check for circular references in account hierarchy
        for (JournalLine line : entry.getJournalLines()) {
            Account account = line.getAccount();
            if (hasCircularReference(account, accounts)) {
                errors = errors.append("Circular reference detected in account hierarchy: " + account.getCode());
            }
        }
        
        // Check for reasonable amounts based on account type
        for (JournalLine line : entry.getJournalLines()) {
            Account account = line.getAccount();
            BigDecimal amount = BigDecimal.valueOf(line.getAmountMinor());
            
            if (isUnreasonableAmount(account, amount)) {
                errors = errors.append(String.format("Amount %s seems unreasonable for account type %s: %s", 
                                                   amount, account.getType(), account.getCode()));
            }
        }
        
        return errors.isEmpty() 
                ? Validation.valid(entry) 
                : Validation.invalid(errors);
    }
    
    /**
     * Checks for circular references in account hierarchy.
     */
    private static boolean hasCircularReference(Account account, List<Account> allAccounts) {
        return hasCircularReferenceHelper(account, allAccounts, List.empty());
    }
    
    private static boolean hasCircularReferenceHelper(Account account, List<Account> allAccounts, List<UUID> visited) {
        if (visited.contains(account.getId())) {
            return true;
        }
        
        if (account.getParentAccount() == null) {
            return false;
        }
        
        List<UUID> newVisited = visited.append(account.getId());
        return hasCircularReferenceHelper(account.getParentAccount(), allAccounts, newVisited);
    }
    
    /**
     * Checks if an amount is reasonable for a given account type.
     */
    private static boolean isUnreasonableAmount(Account account, BigDecimal amount) {
        // This is a simplified example - in practice, you'd have more sophisticated rules
        BigDecimal absAmount = amount.abs();
        
        switch (account.getType()) {
            case ASSET:
                return absAmount.compareTo(new BigDecimal("1000000000")) > 0; // 1 billion
            case LIABILITY:
                return absAmount.compareTo(new BigDecimal("1000000000")) > 0; // 1 billion
            case EQUITY:
                return absAmount.compareTo(new BigDecimal("100000000")) > 0;  // 100 million
            case REVENUE:
                return absAmount.compareTo(new BigDecimal("100000000")) > 0;  // 100 million
            case EXPENSE:
                return absAmount.compareTo(new BigDecimal("10000000")) > 0;   // 10 million
            default:
                return false;
        }
    }
    
    /**
     * Validates that a ledger can be closed considering all related entities.
     */
    public static Validation<Seq<String>, Ledger> validateLedgerClosure(
            Ledger ledger,
            List<Account> accounts,
            List<Period> periods,
            List<JournalEntry> journalEntries) {
        
        List<String> errors = List.empty();
        
        // Check if ledger has any open periods
        boolean hasOpenPeriods = periods
                .exists(p -> p.getLedger().getId().equals(ledger.getId()) && 
                            p.getStatus() == PeriodStatus.OPEN);
        
        if (hasOpenPeriods) {
            errors = errors.append("Cannot close ledger with open periods");
        }
        
        // Check if ledger has any draft journal entries
        boolean hasDraftEntries = journalEntries
                .exists(je -> je.getLedger().getId().equals(ledger.getId()) && 
                             je.getStatus() == JournalEntryStatus.DRAFT);
        
        if (hasDraftEntries) {
            errors = errors.append("Cannot close ledger with draft journal entries");
        }
        
        // Check if ledger has any accounts with non-zero balances
        // Note: This validation is commented out as Account.getBalance() is not yet implemented
        // boolean hasNonZeroBalances = accounts
        //         .exists(acc -> acc.getLedger().getId().equals(ledger.getId()) && 
        //                       acc.getBalance().compareTo(BigDecimal.ZERO) != 0);
        // 
        // if (hasNonZeroBalances) {
        //     errors = errors.append("Cannot close ledger with accounts having non-zero balances");
        // }
        
        return errors.isEmpty() 
                ? Validation.valid(ledger) 
                : Validation.invalid(errors);
    }
    
    /**
     * Validates that an account can be deactivated considering all related entities.
     */
    public static Validation<Seq<String>, Account> validateAccountDeactivation(
            Account account,
            List<JournalEntry> journalEntries,
            List<Period> periods) {
        
        List<String> errors = List.empty();
        
        // Check if account has any recent activity
        LocalDate cutoffDate = LocalDate.now().minusMonths(12);
        
        boolean hasRecentActivity = journalEntries
                .exists(je -> {
                    // Convert journal lines to Vavr List and check for matching account
                    List<JournalLine> lines = io.vavr.collection.List.ofAll(je.getJournalLines());
                    return lines.exists(jl -> 
                        jl.getAccount().getId().equals(account.getId()) &&
                        je.getAccountingDate().isAfter(cutoffDate));
                });
        
        if (hasRecentActivity) {
            errors = errors.append("Cannot deactivate account with recent activity");
        }
        
        // Check if account is in an open period
        boolean inOpenPeriod = periods
                .exists(p -> p.getLedger().getId().equals(account.getLedger().getId()) &&
                            p.getStatus() == PeriodStatus.OPEN);
        
        if (inOpenPeriod) {
            errors = errors.append("Cannot deactivate account during open period");
        }
        
        return errors.isEmpty() 
                ? Validation.valid(account) 
                : Validation.invalid(errors);
    }
}
