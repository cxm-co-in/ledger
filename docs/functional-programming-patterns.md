# Functional Programming Patterns with Vavr

This document demonstrates how to use Vavr functional programming constructs to replace imperative if-else blocks and exception throwing, making the code more elegant and functional.

## Overview

Instead of using imperative patterns like:
```java
if (condition) {
    throw new Exception("Error message");
}
```

We can use functional patterns that are more expressive and chainable.

## 1. Null Safety with Option

### Before (Imperative):
```java
UUID currentTenantId = TenantContext.getCurrentTenant();
if (currentTenantId == null) {
    throw new IllegalStateException("Tenant context not set");
}
```

### After (Functional with Vavr):
```java
UUID currentTenantId = FunctionalUtils.requireNonNull(
    TenantContext.getCurrentTenant(), 
    "Tenant context not set"
);
```

### Alternative using Option directly:
```java
UUID currentTenantId = Option.of(TenantContext.getCurrentTenant())
    .getOrElseThrow(() -> new IllegalStateException("Tenant context not set"));
```

## 2. Repository Lookups with Option

### Before (Imperative):
```java
Ledger ledger = ledgerRepository.findById(ledgerId)
    .orElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
```

### After (Functional with Vavr):
```java
Ledger ledger = Option.ofOptional(ledgerRepository.findById(ledgerId))
    .getOrElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
```

## 3. Validation Checks with requireTrue

### Before (Imperative):
```java
if (!ledger.getTenant().getId().equals(currentTenantId)) {
    throw new IllegalArgumentException("Ledger does not belong to current tenant");
}
```

### After (Functional with Vavr):
```java
FunctionalUtils.requireTrue(
    ledger.getTenant().getId().equals(currentTenantId),
    () -> new IllegalArgumentException("Ledger does not belong to current tenant")
);
```

## 4. Complex Validation with Validation Framework

### Before (Imperative):
```java
BasicValidationResult<PeriodValidator.PeriodCreationRequest> validationResult = 
    PeriodValidator.validateCreation(ledgerId, name, startDate, endDate, status);

if (validationResult.isFailure()) {
    throw new IllegalArgumentException("Validation failed: " + String.join(", ", validationResult.getErrorsAsList()));
}
```

### After (Functional with Vavr):
```java
BasicValidationResult<PeriodValidator.PeriodCreationRequest> validationResult = 
    PeriodValidator.validateCreation(ledgerId, name, startDate, endDate, status);

FunctionalUtils.requireTrue(
    validationResult.isSuccess(),
    () -> new IllegalArgumentException("Validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
);
```

## 5. Advanced Vavr Patterns

### Option for Pattern Matching:
```java
public static String patternMatch(String input) {
    return Option.of(input)
        .filter(s -> s.length() > 10)
        .map(s -> "Long string: " + s)
        .getOrElse(() -> Option.of(input)
            .filter(s -> s.length() > 5)
            .map(s -> "Medium string: " + s)
            .getOrElse("Short string: " + input));
}
```

### Try for Exception Handling:
```java
public static Try<BigDecimal> safeDivision(BigDecimal numerator, BigDecimal denominator) {
    return Try.of(() -> numerator.divide(denominator, 6, BigDecimal.ROUND_HALF_UP))
        .onFailure(ArithmeticException.class, e -> 
            System.err.println("Division by zero attempted"));
}
```

### Validation for Complex Business Rules:
```java
public static Validation<Seq<String>, BigDecimal> validateAmount(BigDecimal amount) {
    return Validation.combine(
        validatePositive(amount),
        validateNotTooLarge(amount),
        validatePrecision(amount)
    ).ap((pos, size, prec) -> amount);
}
```

## 6. Benefits of Functional Approach

### 1. **Readability**
- Code reads more like business logic
- Less visual noise from if-else blocks
- Clear intent of what the code is trying to achieve

### 2. **Composability**
- Functions can be chained together
- Easy to add/remove validation steps
- Reusable validation components

### 3. **Error Handling**
- Errors are handled declaratively
- Exception throwing is centralized
- Better error message consistency

### 4. **Testability**
- Validation logic can be tested independently
- Easier to mock and verify behavior
- Clear separation of concerns

### 5. **Maintainability**
- Changes to validation rules in one place
- Consistent error handling patterns
- Easier to refactor and extend

## 7. Migration Strategy

### Phase 1: Create Utility Functions
- Start with `FunctionalUtils.requireNonNull()`
- Add `FunctionalUtils.requireTrue()`
- Create validation framework

### Phase 2: Refactor Existing Code
- Replace null checks with `requireNonNull()`
- Replace validation if-else with `requireTrue()`
- Update repository lookups to use `Option.ofOptional()`

### Phase 3: Advanced Patterns
- Introduce `Validation` for complex business rules
- Use `Try` for exception handling
- Implement pattern matching with `Option`

## 8. Best Practices

### 1. **Keep Functions Pure**
- Avoid side effects in validation functions
- Return validation results instead of throwing exceptions
- Use functional composition

### 2. **Consistent Error Messages**
- Centralize error message creation
- Use consistent formatting and terminology
- Provide actionable error messages

### 3. **Performance Considerations**
- Use lazy evaluation where appropriate
- Avoid creating unnecessary objects
- Profile critical paths

### 4. **Documentation**
- Document complex validation chains
- Explain business rules clearly
- Provide examples of usage

## 9. Example: Complete Refactored Method

### Before (Imperative):
```java
@Override
@Transactional
public Period createPeriod(UUID ledgerId, String name, LocalDate startDate, LocalDate endDate, PeriodStatus status) {
    // Get tenant from context
    UUID currentTenantId = TenantContext.getCurrentTenant();
    if (currentTenantId == null) {
        throw new IllegalStateException("Tenant context not set");
    }

    // Validate ledger exists and belongs to current tenant
    Ledger ledger = ledgerRepository.findById(ledgerId)
            .orElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
    
    if (!ledger.getTenant().getId().equals(currentTenantId)) {
        throw new IllegalArgumentException("Ledger does not belong to current tenant");
    }

    // Validate business rules
    BasicValidationResult<PeriodValidator.PeriodCreationRequest> validationResult = 
            PeriodValidator.validateCreation(ledgerId, name, startDate, endDate, status);
    
    if (validationResult.isFailure()) {
        throw new IllegalArgumentException("Validation failed: " + String.join(", ", validationResult.getErrorsAsList()));
    }

    // Check for overlapping periods
    List<Period> existingPeriods = periodRepository.findByTenantIdAndLedgerIdAndStatus(
            currentTenantId, ledgerId, PeriodStatus.OPEN);
    
    BasicValidationResult<List<Period>> overlapValidation = 
            PeriodValidator.validateNoOverlap(startDate, endDate, existingPeriods);
    
    if (overlapValidation.isFailure()) {
        throw new IllegalArgumentException(overlapValidation.getErrorsAsList().get(0));
    }

    // Create new period
    Tenant tenant = tenantRepository.findById(currentTenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant", currentTenantId.toString()));

    Period period = Period.builder()
            .tenant(tenant)
            .ledger(ledger)
            .name(name)
            .startDate(startDate)
            .endDate(endDate)
            .status(status)
            .build();

    return periodRepository.save(period);
}
```

### After (Functional with Vavr):
```java
@Override
@Transactional
public Period createPeriod(UUID ledgerId, String name, LocalDate startDate, LocalDate endDate, PeriodStatus status) {
    // Get tenant from context using functional approach
    UUID currentTenantId = FunctionalUtils.requireNonNull(
        TenantContext.getCurrentTenant(), 
        "Tenant context not set"
    );

    // Validate ledger exists and belongs to current tenant using functional approach
    Ledger ledger = Option.ofOptional(ledgerRepository.findById(ledgerId))
            .getOrElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
    
    FunctionalUtils.requireTrue(
        ledger.getTenant().getId().equals(currentTenantId),
        () -> new IllegalArgumentException("Ledger does not belong to current tenant")
    );

    // Use validation framework for business rules
    BasicValidationResult<PeriodValidator.PeriodCreationRequest> validationResult = 
            PeriodValidator.validateCreation(ledgerId, name, startDate, endDate, status);
    
    FunctionalUtils.requireTrue(
        validationResult.isSuccess(),
        () -> new IllegalArgumentException("Validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
    );

    // Check for overlapping periods using validation framework
    List<Period> existingPeriods = periodRepository.findByTenantIdAndLedgerIdAndStatus(
            currentTenantId, ledgerId, PeriodStatus.OPEN);
    
    BasicValidationResult<List<Period>> overlapValidation = 
            PeriodValidator.validateNoOverlap(startDate, endDate, existingPeriods);
    
    FunctionalUtils.requireTrue(
        overlapValidation.isSuccess(),
        () -> new IllegalArgumentException(overlapValidation.getErrorsAsList().get(0))
    );

    // Create new period using functional approach
    Tenant tenant = Option.ofOptional(tenantRepository.findById(currentTenantId))
            .getOrElseThrow(() -> new ResourceNotFoundException("Tenant", currentTenantId.toString()));

    Period period = Period.builder()
            .tenant(tenant)
            .ledger(ledger)
            .name(name)
            .startDate(startDate)
            .endDate(endDate)
            .status(status)
            .build();

    return periodRepository.save(period);
}
```

## 10. Conclusion

Using Vavr functional patterns provides several advantages:

1. **Cleaner Code**: Less visual noise from if-else blocks
2. **Better Error Handling**: Centralized and consistent exception handling
3. **Improved Readability**: Code reads more like business logic
4. **Enhanced Maintainability**: Easier to modify and extend validation rules
5. **Functional Composition**: Chain operations together naturally
6. **Type Safety**: Better compile-time guarantees

The key is to start small with simple utility functions and gradually introduce more advanced patterns as the team becomes comfortable with functional programming concepts.
