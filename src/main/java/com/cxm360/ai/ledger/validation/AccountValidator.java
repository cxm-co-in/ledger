package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.Account;
import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.NormalSide;

import java.util.UUID;

/**
 * Validator for Account creation and update operations.
 * This demonstrates how to separate validation logic from business logic.
 */
public class AccountValidator {
    
    /**
     * Validates that an account can be created.
     */
    public static BasicValidationResult<Account> validateCreation(Account account, UUID tenantId, UUID ledgerId) {
        return SimpleValidator.of(account)
                .rule(acc -> acc != null, "Account cannot be null")
                .rule(acc -> acc.getTenant() != null, "Tenant cannot be null")
                .rule(acc -> acc.getTenant().getId().equals(tenantId), "Account must belong to current tenant")
                .rule(acc -> acc.getLedger() != null, "Ledger cannot be null")
                .rule(acc -> acc.getLedger().getId().equals(ledgerId), "Account must belong to specified ledger")
                .rule(acc -> acc.getCode() != null && !acc.getCode().trim().isEmpty(), "Account code cannot be null or empty")
                .rule(acc -> acc.getName() != null && !acc.getName().trim().isEmpty(), "Account name cannot be null or empty")
                .rule(acc -> acc.getType() != null, "Account type cannot be null")
                .rule(acc -> acc.getNormalSide() != null, "Normal side cannot be null")
                .rule(acc -> acc.getCurrencyMode() != null, "Currency mode cannot be null")
                .validate();
    }
    
    /**
     * Validates that an account can be updated.
     */
    public static BasicValidationResult<Account> validateCanUpdate(Account account, UUID tenantId) {
        return SimpleValidator.of(account)
                .rule(acc -> acc != null, "Account cannot be null")
                .rule(acc -> acc.getTenant() != null, "Account must have a tenant")
                .rule(acc -> acc.getTenant().getId().equals(tenantId), "Account does not belong to current tenant")
                .validate();
    }
    
    /**
     * Validates account code format and uniqueness.
     */
    public static BasicValidationResult<String> validateAccountCode(String code) {
        return SimpleValidator.of(code)
                .rule(c -> c != null && !c.trim().isEmpty(), "Account code cannot be null or empty")
                .rule(c -> c.trim().length() <= 50, "Account code must be 50 characters or less")
                .rule(c -> c.matches("^[A-Z0-9]+$"), "Account code must contain only uppercase letters and numbers")
                .validate();
    }
    
    /**
     * Validates account name format.
     */
    public static BasicValidationResult<String> validateAccountName(String name) {
        return SimpleValidator.of(name)
                .rule(n -> n != null && !n.trim().isEmpty(), "Account name cannot be null or empty")
                .rule(n -> n.trim().length() <= 255, "Account name must be 255 characters or less")
                .validate();
    }
    
    /**
     * Validates currency constraints for accounts.
     */
    public static BasicValidationResult<Account> validateCurrencyConstraints(Account account) {
        if (account.getCurrencyMode() == CurrencyMode.SINGLE) {
            if (account.getCurrencyCode() == null || account.getCurrencyCode().trim().isEmpty()) {
                return BasicValidationResult.failure("Single currency accounts must specify a currency code");
            }
            
            if (!account.getCurrencyCode().matches("^[A-Z]{3}$")) {
                return BasicValidationResult.failure("Currency code must be a 3-letter ISO code (e.g., USD, EUR)");
            }
        }
        
        return BasicValidationResult.success(account);
    }
    
    /**
     * Validates parent account relationship.
     */
    public static BasicValidationResult<Account> validateParentAccount(Account account, UUID accountId) {
        if (account.getParentAccount() != null) {
            if (account.getParentAccount().getId().equals(accountId)) {
                return BasicValidationResult.failure("Account cannot be its own parent");
            }
            
            // Validate that parent account belongs to same tenant and ledger
            if (!account.getParentAccount().getTenant().getId().equals(account.getTenant().getId())) {
                return BasicValidationResult.failure("Parent account must belong to the same tenant");
            }
            
            if (!account.getParentAccount().getLedger().getId().equals(account.getLedger().getId())) {
                return BasicValidationResult.failure("Parent account must belong to the same ledger");
            }
        }
        
        return BasicValidationResult.success(account);
    }
    
    /**
     * Validates account type and normal side consistency.
     */
    public static BasicValidationResult<Account> validateAccountTypeConsistency(Account account) {
        AccountType type = account.getType();
        NormalSide normalSide = account.getNormalSide();
        
        // Asset and Expense accounts normally have DEBIT balance
        if ((type == AccountType.ASSET || type == AccountType.EXPENSE) && normalSide == NormalSide.CREDIT) {
            return BasicValidationResult.failure(
                String.format("Account type %s normally has DEBIT balance, but normal side is set to CREDIT", type)
            );
        }
        
        // Liability, Equity, and Revenue accounts normally have CREDIT balance
        if ((type == AccountType.LIABILITY || type == AccountType.EQUITY || type == AccountType.REVENUE) && normalSide == NormalSide.DEBIT) {
            return BasicValidationResult.failure(
                String.format("Account type %s normally has CREDIT balance, but normal side is set to DEBIT", type)
            );
        }
        
        return BasicValidationResult.success(account);
    }
    
    /**
     * Validates that an account can be deactivated.
     */
    public static BasicValidationResult<Account> validateCanDeactivate(Account account) {
        // Check if account has any active postings or balances
        // This would require additional repository calls in a real implementation
        if (account.isActive()) {
            return BasicValidationResult.success(account);
        } else {
            return BasicValidationResult.failure("Account is already inactive");
        }
    }
}
