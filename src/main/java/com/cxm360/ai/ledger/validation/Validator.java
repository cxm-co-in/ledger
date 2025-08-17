package com.cxm360.ai.ledger.validation;

import io.vavr.control.Validation;

/**
 * Generic validation interface for domain objects.
 * 
 * @param <T> the type to validate
 * @param <E> the error type
 */
@FunctionalInterface
public interface Validator<T, E> {
    
    /**
     * Validates the given input and returns either a success or failure.
     * 
     * @param input the input to validate
     * @return Validation containing either the valid input or validation errors
     */
    Validation<E, T> validate(T input);
    
    /**
     * Creates a validator that always succeeds.
     * 
     * @param <T> the type to validate
     * @return a validator that always succeeds
     */
    static <T> Validator<T, String> alwaysValid() {
        return input -> Validation.valid(input);
    }
    
    /**
     * Creates a validator that always fails with the given error.
     * 
     * @param <T> the type to validate
     * @param error the error message
     * @return a validator that always fails
     */
    static <T> Validator<T, String> alwaysFail(String error) {
        return input -> Validation.invalid(error);
    }
}
