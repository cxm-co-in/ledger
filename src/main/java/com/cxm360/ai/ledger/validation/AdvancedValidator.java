package com.cxm360.ai.ledger.validation;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Advanced validation framework using Vavr's Validation type.
 * This demonstrates sophisticated functional validation patterns including:
 * - Accumulating multiple validation errors
 * - Functional composition of validation rules
 * - Cross-entity validation
 * - Conditional validation chains
 */
public class AdvancedValidator {
    
    /**
     * Creates a validation rule that can be composed with others.
     * Uses Vavr's Validation to accumulate errors.
     */
    public static <T> Validation<String, T> rule(T value, Predicate<T> predicate, String errorMessage) {
        return predicate.test(value) 
            ? Validation.valid(value) 
            : Validation.invalid(errorMessage);
    }
    
    /**
     * Creates a validation rule for nullable values.
     */
    public static <T> Validation<String, T> nullableRule(T value, Predicate<T> predicate, String errorMessage) {
        if (value == null) {
            return Validation.valid(value);
        }
        return rule(value, predicate, errorMessage);
    }
    
    /**
     * Combines multiple validation rules using Vavr's Validation.
     * Accumulates all errors into a single result.
     */
    @SafeVarargs
    public static <T> Validation<Seq<String>, T> combine(T value, Validation<String, T>... validations) {
        Validation<Seq<String>, T> result = Validation.valid(value);
        
        for (Validation<String, T> validation : validations) {
            final Validation<Seq<String>, T> currentResult = result;
            result = result.flatMap(v -> 
                validation.mapError(error -> 
                    currentResult.isValid() 
                        ? io.vavr.collection.List.of(error)
                        : currentResult.getError().append(error)
                )
            );
        }
        
        return result;
    }
    
    /**
     * Creates a conditional validation that only applies if a condition is met.
     */
    public static <T> Validation<String, T> conditionalRule(
            T value, 
            Predicate<T> condition, 
            Predicate<T> validation, 
            String errorMessage) {
        
        if (!condition.test(value)) {
            return Validation.valid(value);
        }
        return rule(value, validation, errorMessage);
    }
    
    /**
     * Validates a value against multiple rules and returns a comprehensive result.
     */
    public static <T> Validation<Seq<String>, T> validateAll(T value, List<ValidationRule<T>> rules) {
        Validation<Seq<String>, T> result = Validation.valid(value);
        
        for (ValidationRule<T> rule : rules) {
            Validation<String, T> validation = rule.apply(value);
            final Validation<Seq<String>, T> currentResult = result;
            result = result.flatMap(v -> 
                validation.mapError(error -> 
                    currentResult.isValid() 
                        ? io.vavr.collection.List.of(error)
                        : currentResult.getError().append(error)
                )
            );
        }
        
        return result;
    }
    
    /**
     * Creates a cross-entity validation that depends on another entity's validation.
     */
    public static <T, U> Validation<String, T> crossEntityValidation(
            T value,
            U relatedEntity,
            Validation<String, U> relatedValidation,
            Function<U, Predicate<T>> validationPredicate,
            String errorMessage) {
        
        return relatedValidation.flatMap(validEntity -> {
            Predicate<T> predicate = validationPredicate.apply(validEntity);
            return rule(value, predicate, errorMessage);
        });
    }
    
    /**
     * Functional interface for validation rules.
     */
    @FunctionalInterface
    public interface ValidationRule<T> {
        Validation<String, T> apply(T value);
    }
    
    /**
     * Builder pattern for creating complex validation chains.
     */
    public static class ValidationBuilder<T> {
        private final T value;
        private final io.vavr.collection.List<ValidationRule<T>> rules = io.vavr.collection.List.empty();
        
        private ValidationBuilder(T value) {
            this.value = value;
        }
        
        public static <T> ValidationBuilder<T> of(T value) {
            return new ValidationBuilder<>(value);
        }
        
        public ValidationBuilder<T> rule(Predicate<T> predicate, String errorMessage) {
            ValidationRule<T> rule = val -> AdvancedValidator.rule(val, predicate, errorMessage);
            return new ValidationBuilder<>(value) {
                @Override
                public io.vavr.collection.List<ValidationRule<T>> getRules() {
                    return rules.append(rule);
                }
            };
        }
        
        public ValidationBuilder<T> nullableRule(Predicate<T> predicate, String errorMessage) {
            ValidationRule<T> rule = val -> AdvancedValidator.nullableRule(val, predicate, errorMessage);
            return new ValidationBuilder<>(value) {
                @Override
                public io.vavr.collection.List<ValidationRule<T>> getRules() {
                    return rules.append(rule);
                }
            };
        }
        
        public ValidationBuilder<T> conditionalRule(
                Predicate<T> condition, 
                Predicate<T> validation, 
                String errorMessage) {
            ValidationRule<T> rule = val -> AdvancedValidator.conditionalRule(val, condition, validation, errorMessage);
            return new ValidationBuilder<>(value) {
                @Override
                public io.vavr.collection.List<ValidationRule<T>> getRules() {
                    return rules.append(rule);
                }
            };
        }
        
        public Validation<Seq<String>, T> validate() {
            return AdvancedValidator.validateAll(value, rules.toJavaList());
        }
        
        protected io.vavr.collection.List<ValidationRule<T>> getRules() {
            return rules;
        }
    }
    
    /**
     * Utility method to convert Validation result to BasicValidationResult for backward compatibility.
     */
    public static <T> BasicValidationResult<T> toBasicResult(Validation<Seq<String>, T> validation) {
        if (validation.isValid()) {
            return BasicValidationResult.success(validation.get());
        } else {
            return BasicValidationResult.failure(validation.getError().toJavaList());
        }
    }
    
    /**
     * Utility method to convert BasicValidationResult to Validation for advanced operations.
     */
    public static <T> Validation<Seq<String>, T> fromBasicResult(BasicValidationResult<T> result) {
        if (result.isSuccess()) {
            return Validation.valid(result.getValue());
        } else {
            return Validation.invalid(io.vavr.collection.List.ofAll(result.getErrorsAsList()));
        }
    }
}
