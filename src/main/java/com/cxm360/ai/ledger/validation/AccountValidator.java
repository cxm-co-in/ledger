package com.cxm360.ai.ledger.validation;

import com.cxm360.ai.ledger.model.Account;
import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import io.vavr.control.Option;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Validator for Account creation and update operations.
 * This demonstrates how to separate validation logic from business logic.
 */
public class AccountValidator {
    
    /**
     * Validates that an account can be created using functional style.
     */
    public static BasicValidationResult<Account> validateCreation(Account account, UUID tenantId, UUID ledgerId) {
        return Option.of(account)
                .filter(acc -> acc != null)
                .filter(acc -> acc.getTenant() != null)
                .filter(acc -> acc.getTenant().getId().equals(tenantId))
                .filter(acc -> acc.getLedger() != null)
                .filter(acc -> acc.getLedger().getId().equals(ledgerId))
                .filter(acc -> StringUtils.hasText(acc.getCode()))
                .filter(acc -> StringUtils.hasText(acc.getName()))
                .filter(acc -> acc.getType() != null)
                .filter(acc -> acc.getNormalSide() != null)
                .filter(acc -> acc.getCurrencyMode() != null)
                .map(acc -> BasicValidationResult.success(acc))
                .getOrElse(() -> BasicValidationResult.failure("Account creation validation failed - check required fields"));
    }
    
    /**
     * Validates that an account can be updated using functional style.
     */
    public static BasicValidationResult<Account> validateCanUpdate(Account account, UUID tenantId) {
        return Option.of(account)
                .filter(acc -> acc != null)
                .filter(acc -> acc.getTenant() != null)
                .filter(acc -> acc.getTenant().getId().equals(tenantId))
                .map(acc -> BasicValidationResult.success(acc))
                .getOrElse(() -> BasicValidationResult.failure("Account update validation failed - check account existence and tenant ownership"));
    }
    
    /**
     * Validates account code format and uniqueness using functional style.
     */
    public static BasicValidationResult<String> validateAccountCode(String code) {
        return Option.of(code)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(c -> c.length() <= 50)
                .filter(c -> c.matches("^[A-Z0-9]+$"))
                .map(c -> BasicValidationResult.success(c))
                .getOrElse(() -> BasicValidationResult.failure("Account code validation failed"));
    }
    
    /**
     * Validates account name format using functional style.
     */
    public static BasicValidationResult<String> validateAccountName(String name) {
        return Option.of(name)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(n -> n.length() <= 255)
                .map(n -> BasicValidationResult.success(n))
                .getOrElse(() -> BasicValidationResult.failure("Account name validation failed"));
    }
    
    /**
     * Validates currency constraints for accounts using functional style.
     */
    public static BasicValidationResult<Account> validateCurrencyConstraints(Account account) {
        return Option.of(account)
                .filter(acc -> acc.getCurrencyMode() == CurrencyMode.SINGLE)
                .map(acc -> validateSingleCurrencyAccount(acc))
                .getOrElse(() -> BasicValidationResult.success(account));
    }

    /**
     * Validates single currency account constraints using functional composition.
     */
    private static BasicValidationResult<Account> validateSingleCurrencyAccount(Account account) {
        return Option.of(account.getCurrencyCode())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(isValidCurrencyCode())
                .map(code -> BasicValidationResult.success(account))
                .getOrElse(() -> BasicValidationResult.failure("Single currency accounts must specify a currency code"));
    }

    /**
     * Creates a predicate to validate currency code format.
     */
    private static Predicate<String> isValidCurrencyCode() {
        return code -> code.matches("^[A-Z]{3}$");
    }
    
    /**
     * Validates parent account relationship using functional style.
     */
    public static BasicValidationResult<Account> validateParentAccount(Account account, UUID accountId) {
        return Option.of(account.getParentAccount())
                .map(parent -> validateParentAccountConstraints(account, parent, accountId))
                .getOrElse(() -> BasicValidationResult.success(account));
    }

    /**
     * Validates parent account constraints using functional composition.
     */
    private static BasicValidationResult<Account> validateParentAccountConstraints(Account account, Account parent, UUID accountId) {
        return Option.of(parent)
                .filter(p -> !p.getId().equals(accountId))
                .map(p -> validateParentAccountTenancy(account, p))
                .getOrElse(() -> BasicValidationResult.failure("Account cannot be its own parent"));
    }

    /**
     * Validates parent account tenancy and ledger consistency.
     */
    private static BasicValidationResult<Account> validateParentAccountTenancy(Account account, Account parent) {
        return Option.of(parent)
                .filter(p -> p.getTenant().getId().equals(account.getTenant().getId()))
                .filter(p -> p.getLedger().getId().equals(account.getLedger().getId()))
                .map(p -> BasicValidationResult.success(account))
                .getOrElse(() -> BasicValidationResult.failure("Parent account must belong to the same tenant and ledger"));
    }
    
    /**
     * Validates account type and normal side consistency using functional style.
     */
    public static BasicValidationResult<Account> validateAccountTypeConsistency(Account account) {
        return Option.of(account)
                .map(acc -> validateAccountTypeBalance(acc))
                .getOrElse(() -> BasicValidationResult.success(account));
    }

    /**
     * Validates account type balance consistency using functional composition.
     */
    private static BasicValidationResult<Account> validateAccountTypeBalance(Account account) {
        return validateDebitNormalAccount(account)
                .orElse(() -> validateCreditNormalAccount(account))
                .getOrElse(() -> BasicValidationResult.success(account));
    }

    /**
     * Validates DEBIT normal accounts for consistency.
     */
    private static Option<BasicValidationResult<Account>> validateDebitNormalAccount(Account account) {
        return Option.of(account)
                .filter(acc -> isDebitNormalAccount(acc.getType()) && acc.getNormalSide() == NormalSide.CREDIT)
                .map(acc -> BasicValidationResult.failure(
                    String.format("Account type %s normally has DEBIT balance, but normal side is set to CREDIT", acc.getType())
                ));
    }

    /**
     * Validates CREDIT normal accounts for consistency.
     */
    private static Option<BasicValidationResult<Account>> validateCreditNormalAccount(Account account) {
        return Option.of(account)
                .filter(acc -> isCreditNormalAccount(acc.getType()) && acc.getNormalSide() == NormalSide.DEBIT)
                .map(acc -> BasicValidationResult.failure(
                    String.format("Account type %s normally has CREDIT balance, but normal side is set to DEBIT", acc.getType())
                ));
    }

    /**
     * Checks if account type normally has DEBIT balance.
     */
    private static boolean isDebitNormalAccount(AccountType type) {
        return type == AccountType.ASSET || type == AccountType.EXPENSE;
    }

    /**
     * Checks if account type normally has CREDIT balance.
     */
    private static boolean isCreditNormalAccount(AccountType type) {
        return type == AccountType.LIABILITY || type == AccountType.EQUITY || type == AccountType.REVENUE;
    }
    
    /**
     * Validates that an account can be deactivated using functional style.
     */
    public static BasicValidationResult<Account> validateCanDeactivate(Account account) {
        return Option.of(account)
                .filter(Account::isActive)
                .map(acc -> BasicValidationResult.success(acc))
                .getOrElse(() -> BasicValidationResult.failure("Account is already inactive"));
    }
}
