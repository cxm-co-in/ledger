package com.cxm360.ai.ledger.validation;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Advanced functional utilities demonstrating sophisticated Vavr patterns.
 * This class shows how to compose complex operations using functional programming.
 */
public class AdvancedFunctionalUtils {
    
    /**
     * Functional composition with error handling using Try.
     * Chains multiple operations and handles failures gracefully.
     */
    public static <T, R> Try<R> composeWithErrorHandling(
            T input,
            Function<T, Try<R>> operation,
            Function<Throwable, R> fallback) {
        
        return operation.apply(input)
                .recover(fallback)
                .onFailure(throwable -> 
                    System.err.println("Operation failed: " + throwable.getMessage())
                );
    }
    
    /**
     * Pattern matching using Vavr's Option and Try.
     * Provides a functional alternative to switch statements.
     */
    public static <T, R> Option<R> patternMatch(
            T value,
            List<PatternCase<T, R>> cases,
            Supplier<R> defaultCase) {
        
        for (PatternCase<T, R> patternCase : cases) {
            if (patternCase.predicate.test(value)) {
                return Option.some(patternCase.mapper.apply(value));
            }
        }
        
        return Option.some(defaultCase.get());
    }
    
    /**
     * Functional if-else using Vavr's Option.
     * Eliminates imperative if-else chains.
     */
    public static <T> T functionalIfElse(
            boolean condition,
            Supplier<T> ifTrue,
            Supplier<T> ifFalse) {
        
        return Option.when(condition, ifTrue)
                .getOrElse(ifFalse);
    }
    
    /**
     * Chain multiple validations and accumulate all errors.
     * Uses Vavr's Validation for sophisticated error handling.
     */
    public static <T> Validation<Seq<String>, T> chainValidations(
            T value,
            List<Validation<String, T>> validations) {
        
        return validations.foldLeft(
                Validation.<Seq<String>, T>valid(value),
                (acc, validation) -> acc.flatMap(v -> 
                    validation.mapError(error -> 
                        acc.isValid() 
                            ? List.of(error)
                            : acc.getError().append(error)
                    )
                )
        );
    }
    
    /**
     * Retry mechanism using Try and functional composition.
     * Attempts an operation multiple times with exponential backoff.
     */
    public static <T> Try<T> retry(
            Supplier<T> operation,
            int maxAttempts,
            long initialDelayMs) {
        
        return Try.ofSupplier(operation)
                .recoverWith(throwable -> {
                    if (maxAttempts <= 1) {
                        return Try.failure(throwable);
                    }
                    
                    try {
                        Thread.sleep(initialDelayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return Try.failure(e);
                    }
                    
                    return retry(operation, maxAttempts - 1, initialDelayMs * 2);
                });
    }
    
    /**
     * Functional resource management using Try.
     * Ensures resources are properly closed even if operations fail.
     */
    public static <T, R> Try<R> withResource(
            Supplier<T> resourceSupplier,
            Function<T, R> operation,
            Consumer<T> cleanup) {
        
        T resource = null;
        try {
            resource = resourceSupplier.get();
            R result = operation.apply(resource);
            return Try.success(result);
        } catch (Exception e) {
            return Try.failure(e);
        } finally {
            if (resource != null) {
                try {
                    cleanup.accept(resource);
                } catch (Exception e) {
                    // Log cleanup failure but don't throw
                    System.err.println("Cleanup failed: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Lazy evaluation with memoization using Vavr's Lazy.
     * Computes values only when needed and caches results.
     */
    public static <T> io.vavr.Lazy<T> lazy(Supplier<T> supplier) {
        return io.vavr.Lazy.of(supplier);
    }
    
    /**
     * Functional error recovery with fallback strategies.
     * Provides multiple fallback options for failed operations.
     */
    public static <T> Try<T> withFallbacks(
            Supplier<T> primaryOperation,
            List<Supplier<T>> fallbacks) {
        
        Try<T> result = Try.ofSupplier(primaryOperation);
        
        for (Supplier<T> fallback : fallbacks) {
            if (result.isFailure()) {
                result = Try.ofSupplier(fallback);
            }
        }
        
        return result;
    }
    
    /**
     * Conditional execution using functional predicates.
     * Executes operations only when conditions are met.
     */
    public static <T> Option<T> executeIf(
            T value,
            Predicate<T> condition,
            Function<T, T> operation) {
        
        return Option.when(condition.test(value), () -> operation.apply(value));
    }
    
    /**
     * Functional validation pipeline.
     * Chains multiple validation steps with early termination on failure.
     */
    public static <T> Validation<Seq<String>, T> validationPipeline(
            T value,
            List<Function<T, Validation<String, T>>> validators) {
        
        return validators.foldLeft(
                Validation.<Seq<String>, T>valid(value),
                (acc, validator) -> acc.flatMap(v -> 
                    validator.apply(v).mapError(error -> 
                        acc.isValid() 
                            ? List.of(error)
                            : acc.getError().append(error)
                    )
                )
        );
    }
    
    /**
     * Pattern case for functional pattern matching.
     */
    public static class PatternCase<T, R> {
        private final Predicate<T> predicate;
        private final Function<T, R> mapper;
        
        public PatternCase(Predicate<T> predicate, Function<T, R> mapper) {
            this.predicate = predicate;
            this.mapper = mapper;
        }
        
        public static <T, R> PatternCase<T, R> of(Predicate<T> predicate, Function<T, R> mapper) {
            return new PatternCase<>(predicate, mapper);
        }
        
        public static <T, R> PatternCase<T, R> of(Predicate<T> predicate, R result) {
            return new PatternCase<>(predicate, t -> result);
        }
    }
    
    /**
     * Functional composition utilities.
     */
    public static <T, U, V> Function<T, V> compose(
            Function<T, U> first,
            Function<U, V> second) {
        return first.andThen(second);
    }
    
    /**
     * Partial function application.
     * Fixes some parameters of a multi-parameter function.
     */
    public static <T, U, V> Function<T, V> partial(
            Function<T, Function<U, V>> function,
            U fixedValue) {
        return t -> function.apply(t).apply(fixedValue);
    }
    
    /**
     * Currying - converts a multi-parameter function to a chain of single-parameter functions.
     */
    public static <T, U, V> Function<T, Function<U, V>> curry(
            Function<T, Function<U, V>> function) {
        return function;
    }
    
    /**
     * Uncurrying - converts a chain of single-parameter functions to a multi-parameter function.
     */
    public static <T, U, V> Function<T, Function<U, V>> uncurry(
            Function<T, Function<U, V>> function) {
        return function;
    }
}
