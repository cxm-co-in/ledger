package com.cxm360.ai.ledger.validation;

import java.util.List;

/**
 * A simple validation result that can hold either a value or a list of errors.
 * 
 * @param <T> the type of the validated value
 */
public class BasicValidationResult<T> {
    
    private final T value;
    private final List<String> errors;
    private final boolean success;
    
    private BasicValidationResult(T value, List<String> errors, boolean success) {
        this.value = value;
        this.errors = errors;
        this.success = success;
    }
    
    /**
     * Creates a successful validation result.
     */
    public static <T> BasicValidationResult<T> success(T value) {
        return new BasicValidationResult<>(value, null, true);
    }
    
    /**
     * Creates a failed validation result.
     */
    public static <T> BasicValidationResult<T> failure(List<String> errors) {
        return new BasicValidationResult<>(null, errors, false);
    }
    
    /**
     * Creates a failed validation result with a single error.
     */
    public static <T> BasicValidationResult<T> failure(String error) {
        return new BasicValidationResult<>(null, List.of(error), false);
    }
    
    /**
     * Checks if the validation was successful.
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Checks if the validation failed.
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * Gets the validated value if successful.
     */
    public T getValue() {
        if (!success) {
            throw new IllegalStateException("Cannot get value from failed validation");
        }
        return value;
    }
    
    /**
     * Gets the validation errors if failed.
     */
    public List<String> getErrors() {
        if (success) {
            throw new IllegalStateException("Cannot get errors from successful validation");
        }
        return errors;
    }
    
    /**
     * Gets the validation errors as a List.
     */
    public List<String> getErrorsAsList() {
        return getErrors();
    }
}
