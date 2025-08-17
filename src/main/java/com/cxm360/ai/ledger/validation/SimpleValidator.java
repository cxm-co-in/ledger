package com.cxm360.ai.ledger.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A simple validation framework for domain objects.
 * This provides a clean way to separate validation logic from business logic.
 */
public class SimpleValidator<T> {
    
    private final List<ValidationRule<T>> rules = new ArrayList<>();
    private final T value;
    
    private SimpleValidator(T value) {
        this.value = value;
    }
    
    /**
     * Creates a new validator for the given value.
     */
    public static <T> SimpleValidator<T> of(T value) {
        return new SimpleValidator<>(value);
    }
    
    /**
     * Adds a validation rule.
     */
    public SimpleValidator<T> rule(Predicate<T> predicate, String errorMessage) {
        rules.add(new ValidationRule<>(predicate, errorMessage));
        return this;
    }
    
    /**
     * Validates the value against all rules.
     */
    public BasicValidationResult<T> validate() {
        List<String> errors = new ArrayList<>();
        
        for (ValidationRule<T> rule : rules) {
            if (!rule.predicate.test(value)) {
                errors.add(rule.errorMessage);
            }
        }
        
        if (errors.isEmpty()) {
            return BasicValidationResult.success(value);
        } else {
            return BasicValidationResult.failure(errors);
        }
    }
    
    /**
     * Throws an exception if validation fails.
     */
    public T validateOrThrow() {
        BasicValidationResult<T> result = validate();
        if (result.isFailure()) {
            throw new ValidationException("Validation failed: " + String.join(", ", result.getErrorsAsList()));
        }
        return result.getValue();
    }
    
    /**
     * A validation rule with a predicate and error message.
     */
    private static class ValidationRule<T> {
        final Predicate<T> predicate;
        final String errorMessage;
        
        ValidationRule(Predicate<T> predicate, String errorMessage) {
            this.predicate = predicate;
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * Exception thrown when validation fails.
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
