package com.cxm360.ai.ledger.validation;

import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Examples demonstrating various Vavr functional patterns for validation and error handling.
 * These patterns can replace imperative if-else blocks and exception throwing.
 */
public class FunctionalValidationExamples {
    
    /**
     * Example 1: Using Option for null-safe operations
     */
    public static String safeStringOperation(String input) {
        return Option.of(input)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "Processed: " + s)
                .getOrElse("Default value");
    }
    
    /**
     * Example 2: Using Try for exception handling
     */
    public static Try<BigDecimal> safeDivision(BigDecimal numerator, BigDecimal denominator) {
        return Try.of(() -> numerator.divide(denominator, 6, BigDecimal.ROUND_HALF_UP))
                .onFailure(ArithmeticException.class, e -> 
                    System.err.println("Division by zero attempted"));
    }
    
    /**
     * Example 3: Using Validation for complex validation chains
     */
    public static Validation<Seq<String>, BigDecimal> validateAmount(BigDecimal amount) {
        return Validation.combine(
                validatePositive(amount),
                validateNotTooLarge(amount),
                validatePrecision(amount)
        ).ap((pos, size, prec) -> amount);
    }
    
    private static Validation<String, BigDecimal> validatePositive(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0 
                ? Validation.valid(amount)
                : Validation.invalid("Amount must be positive");
    }
    
    private static Validation<String, BigDecimal> validateNotTooLarge(BigDecimal amount) {
        return amount.compareTo(new BigDecimal("999999999.99")) <= 0
                ? Validation.valid(amount)
                : Validation.invalid("Amount too large");
    }
    
    private static Validation<String, BigDecimal> validatePrecision(BigDecimal amount) {
        return amount.scale() <= 2
                ? Validation.valid(amount)
                : Validation.invalid("Amount has too many decimal places");
    }
    
    /**
     * Example 4: Functional if-else with Option
     */
    public static String functionalIfElse(boolean condition, String ifTrue, String ifFalse) {
        return Option.when(condition, ifTrue)
                .getOrElse(ifFalse);
    }
    
    /**
     * Example 5: Pattern matching with Option
     */
    public static String patternMatch(String input) {
        return Option.of(input)
                .filter(s -> s.length() > 10)
                .map(s -> "Long string: " + s)
                .getOrElse(() -> Option.of(input)
                        .filter(s -> s.length() > 5)
                        .map(s -> "Medium string: " + s)
                        .getOrElse("Short string: " + input));
    }
    
    /**
     * Example 6: Chaining operations with error handling
     */
    public static Try<String> chainOperations(String input) {
        return Try.of(() -> input)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase())
                .map(s -> "Result: " + s)
                .onSuccess(result -> System.out.println("Operation successful: " + result))
                .onFailure(error -> System.err.println("Operation failed: " + error.getMessage()));
    }
    
    /**
     * Example 7: Using Validation for business rule validation
     */
    public static Validation<Seq<String>, LocalDate> validateDateRange(LocalDate startDate, LocalDate endDate) {
        return Validation.combine(
                validateStartDate(startDate),
                validateEndDate(endDate),
                validateDateOrder(startDate, endDate)
        ).ap((start, end, order) -> endDate);
    }
    
    private static Validation<String, LocalDate> validateStartDate(LocalDate startDate) {
        return startDate != null && !startDate.isBefore(LocalDate.of(2020, 1, 1))
                ? Validation.valid(startDate)
                : Validation.invalid("Start date must be after 2020-01-01");
    }
    
    private static Validation<String, LocalDate> validateEndDate(LocalDate endDate) {
        return endDate != null && !endDate.isAfter(LocalDate.of(2030, 12, 31))
                ? Validation.valid(endDate)
                : Validation.invalid("End date must be before 2030-12-31");
    }
    
    private static Validation<String, Boolean> validateDateOrder(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && !startDate.isAfter(endDate)
                ? Validation.valid(true)
                : Validation.invalid("Start date must be before or equal to end date");
    }
    
    /**
     * Example 8: Using Predicate for validation
     */
    public static <T> Validation<String, T> validateWithPredicate(T value, Predicate<T> predicate, String errorMessage) {
        return predicate.test(value)
                ? Validation.valid(value)
                : Validation.invalid(errorMessage);
    }
    
    /**
     * Example 9: Functional error accumulation
     */
    public static Validation<Seq<String>, String> validateString(String input) {
        return Validation.combine(
                validateWithPredicate(input, s -> s != null, "String cannot be null"),
                validateWithPredicate(input, s -> !s.trim().isEmpty(), "String cannot be empty"),
                validateWithPredicate(input, s -> s.length() <= 100, "String too long (max 100 chars)")
        ).ap((nullCheck, emptyCheck, lengthCheck) -> input);
    }
}
