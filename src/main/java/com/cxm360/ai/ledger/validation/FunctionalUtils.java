package com.cxm360.ai.ledger.validation;

import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class providing functional patterns using Vavr for common validation and error handling scenarios.
 * This helps eliminate imperative if-else blocks and makes code more functional and readable.
 */
public class FunctionalUtils {
    
    /**
     * Safely unwrap an Option, throwing an exception if empty.
     * 
     * @param option the Option to unwrap
     * @param exceptionSupplier supplier for the exception to throw if empty
     * @param <T> the type of the value
     * @param <E> the type of the exception
     * @return the unwrapped value
     * @throws E if the Option is empty
     */
    public static <T, E extends RuntimeException> T getOrElseThrow(Option<T> option, Supplier<E> exceptionSupplier) {
        return option.getOrElseThrow(exceptionSupplier);
    }
    
    /**
     * Safely unwrap an Option, throwing an exception with a message if empty.
     * 
     * @param option the Option to unwrap
     * @param message the error message
     * @param <T> the type of the value
     * @return the unwrapped value
     * @throws IllegalStateException if the Option is empty
     */
    public static <T> T getOrElseThrow(Option<T> option, String message) {
        return option.getOrElseThrow(() -> new IllegalStateException(message));
    }
    
    /**
     * Convert a nullable value to an Option and unwrap it safely.
     * 
     * @param value the nullable value
     * @param message the error message if null
     * @param <T> the type of the value
     * @return the unwrapped value
     * @throws IllegalStateException if the value is null
     */
    public static <T> T requireNonNull(T value, String message) {
        return Option.of(value).getOrElseThrow(() -> new IllegalStateException(message));
    }
    
    /**
     * Validate a condition and throw an exception if false.
     * 
     * @param condition the condition to validate
     * @param message the error message if condition is false
     * @throws IllegalStateException if the condition is false
     */
    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
    
    /**
     * Validate a condition and throw an exception if false.
     * 
     * @param condition the condition to validate
     * @param exceptionSupplier supplier for the exception to throw if false
     * @param <E> the type of the exception
     * @throws E if the condition is false
     */
    public static <E extends RuntimeException> void requireTrue(boolean condition, Supplier<E> exceptionSupplier) {
        if (!condition) {
            throw exceptionSupplier.get();
        }
    }
    
    /**
     * Functional if-else using Vavr.
     * 
     * @param condition the condition to evaluate
     * @param ifTrue the value to return if condition is true
     * @param ifFalse the value to return if condition is false
     * @param <T> the type of the values
     * @return ifTrue if condition is true, otherwise ifFalse
     */
    public static <T> T when(boolean condition, T ifTrue, T ifFalse) {
        return condition ? ifTrue : ifFalse;
    }
    
    /**
     * Functional if-else using Vavr with suppliers for lazy evaluation.
     * 
     * @param condition the condition to evaluate
     * @param ifTrue supplier for the value to return if condition is true
     * @param ifFalse supplier for the value to return if condition is false
     * @param <T> the type of the values
     * @return result of ifTrue supplier if condition is true, otherwise result of ifFalse supplier
     */
    public static <T> T when(boolean condition, Supplier<T> ifTrue, Supplier<T> ifFalse) {
        return condition ? ifTrue.get() : ifFalse.get();
    }
    
    /**
     * Chain validation checks using Vavr Validation.
     * 
     * @param value the value to validate
     * @param validators array of validation functions
     * @param <T> the type of the value
     * @param <E> the type of validation errors
     * @return Validation result
     */
    @SafeVarargs
    public static <T, E> Validation<E, T> chainValidations(T value, Function<T, Validation<E, T>>... validators) {
        Validation<E, T> result = Validation.valid(value);
        for (Function<T, Validation<E, T>> validator : validators) {
            result = result.flatMap(validator);
        }
        return result;
    }
    
    /**
     * Execute a function and wrap the result in a Try.
     * 
     * @param supplier the function to execute
     * @param <T> the type of the result
     * @return Try containing the result or failure
     */
    public static <T> Try<T> tryOf(Supplier<T> supplier) {
        return Try.ofSupplier(supplier);
    }
    

}
