# Tenant Context Usage Guide

## Overview

The general ledger system uses a thread-local tenant context to automatically manage tenant information without requiring it to be passed as a parameter to every method. This provides a clean API while maintaining proper multi-tenancy.

## How It Works

### 1. Tenant Context Filter

The `TenantContextFilter` automatically extracts the `X-Tenant-ID` header from HTTP requests and sets it in the thread-local context.

```java
// The filter automatically sets tenant context from this header
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

### 2. Service Layer Usage

Services can access the current tenant context without requiring it as a parameter:

```java
@Service
public class AccountServiceImpl implements AccountService {
    
    @Override
    @Transactional
    public AccountDto createAccount(UUID ledgerId, CreateAccountRequest request) {
        // Get tenant from context automatically
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        
        // Use the tenant ID for operations
        // ...
    }
}
```

### 3. Controller Layer

Controllers no longer need to extract and pass tenant IDs:

```java
@RestController
public class AccountController {
    
    @PostMapping("/ledgers/{ledgerId}/accounts")
    public ResponseEntity<AccountDto> createAccount(
            @PathVariable UUID ledgerId,
            @Valid @RequestBody CreateAccountRequest request) {
        // Tenant context is automatically set by the filter
        AccountDto createdAccount = accountService.createAccount(ledgerId, request);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }
}
```

## API Endpoints

### Tenant Management

#### Create Tenant
```bash
POST /api/v1/tenants
Content-Type: application/json

{
  "name": "Acme Corporation",
  "settings": "{\"timezone\": \"UTC\", \"defaultCurrency\": \"USD\"}"
}
```

**Note**: Creating a tenant doesn't require the `X-Tenant-ID` header since it's creating the tenant itself.

#### Get Tenant
```bash
GET /api/v1/tenants/{tenantId}
```

#### List All Tenants
```bash
GET /api/v1/tenants
```

#### Update Tenant
```bash
PUT /api/v1/tenants/{tenantId}
Content-Type: application/json

{
  "name": "Acme Corporation Updated",
  "settings": "{\"timezone\": \"UTC\", \"defaultCurrency\": \"EUR\"}"
}
```

### Ledger Management

#### Create Ledger
```bash
POST /api/v1/ledgers
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "name": "USA Operations",
  "functionalCurrencyCode": "USD",
  "timezone": "America/New_York",
  "settings": "{\"revaluationMethod\": \"PERIOD_END\"}"
}
```

#### Get Ledger
```bash
GET /api/v1/ledgers/{ledgerId}
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### List Ledgers for Current Tenant
```bash
GET /api/v1/ledgers
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### Update Ledger
```bash
PUT /api/v1/ledgers/{ledgerId}
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "name": "USA Operations Updated",
  "functionalCurrencyCode": "EUR",
  "timezone": "Europe/London",
  "settings": "{\"revaluationMethod\": \"DAILY\"}"
}
```

### Party Management

#### Create Party
```bash
POST /api/v1/parties
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "name": "Acme Corp",
  "type": "CUSTOMER",
  "externalId": "CRM-001",
  "contactDetails": "{\"email\": \"contact@acme.com\", \"phone\": \"+1-555-0123\"}"
}
```

#### Get Party
```bash
GET /api/v1/parties/{partyId}
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### List Parties
```bash
# All parties for current tenant
GET /api/v1/parties
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

# Parties by type
GET /api/v1/parties?type=CUSTOMER
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

# Search parties by name
GET /api/v1/parties?search=acme
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### Update Party
```bash
PUT /api/v1/parties/{partyId}
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "name": "Acme Corp Updated",
  "type": "VENDOR",
  "externalId": "CRM-002",
  "contactDetails": "{\"email\": \"newcontact@acme.com\", \"phone\": \"+1-555-9999\"}"
}
```

### Account Management

#### Create Account
```bash
POST /api/v1/ledgers/{ledgerId}/accounts
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "code": "1010",
  "name": "Cash on Hand",
  "type": "ASSET",
  "normalSide": "DEBIT",
  "currencyMode": "SINGLE",
  "currencyCode": "USD",
  "isActive": true
}
```

#### Get Account
```bash
GET /api/v1/accounts/{accountId}
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### List Accounts by Ledger
```bash
GET /api/v1/ledgers/{ledgerId}/accounts
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### Update Account
```bash
PUT /api/v1/accounts/{accountId}
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "name": "Cash on Hand Updated",
  "type": "ASSET",
  "normalSide": "DEBIT",
  "currencyMode": "MULTI",
  "currencyCode": null,
  "isActive": true,
  "parentAccountId": "parent-account-uuid"
}
```

### Journal Entry Management

#### Create Journal Entry
```bash
POST /api/v1/ledgers/{ledgerId}/journal-entries
Content-Type: application/json
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "accountingDate": "2025-01-15",
  "description": "Office supplies purchase",
  "journalLines": [
    {
      "accountId": "789e0123-e89b-12d3-a456-426614174000",
      "partyId": "abc-def4-e89b-12d3-a456-426614174000",
      "direction": "DEBIT",
      "currencyCode": "USD",
      "amountMinor": 5000,
      "memo": "Office supplies"
    },
    {
      "accountId": "def-5678-e89b-12d3-a456-426614174000",
      "partyId": "abc-def4-e89b-12d3-a456-426614174000",
      "direction": "CREDIT",
      "currencyCode": "USD",
      "amountMinor": 5000,
      "memo": "Accounts payable"
    }
  ]
}
```

#### Get Journal Entry
```bash
GET /api/v1/journal-entries/{journalEntryId}
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

#### Post Journal Entry
```bash
POST /api/v1/journal-entries/{journalEntryId}/post
X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000
```

## Complete Workflow Example

### 1. Create a Tenant
```bash
curl -X POST "http://localhost:8080/api/v1/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corporation",
    "settings": "{\"timezone\": \"UTC\"}"
  }'
```

**Response**: `{"id": "123e4567-e89b-12d3-a456-426614174000", "name": "Acme Corporation", ...}`

### 2. Create a Ledger
```bash
curl -X POST "http://localhost:8080/api/v1/ledgers" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "name": "USA Operations",
    "functionalCurrencyCode": "USD",
    "timezone": "America/New_York"
  }'
```

**Response**: `{"id": "456e7890-e89b-12d3-a456-426614174000", "tenant": {...}, ...}`

### 3. Create Parties
```bash
curl -X POST "http://localhost:8080/api/v1/parties" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "name": "Office Supplies Inc",
    "type": "VENDOR",
    "externalId": "VENDOR-001",
    "contactDetails": "{\"email\": \"orders@officesupplies.com\"}"
  }'
```

**Response**: `{"id": "789e0123-e89b-12d3-a456-426614174000", "tenant": {...}, ...}`

### 4. Create Accounts
```bash
curl -X POST "http://localhost:8080/api/v1/ledgers/456e7890-e89b-12d3-a456-426614174000/accounts" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "code": "1010",
    "name": "Cash on Hand",
    "type": "ASSET",
    "normalSide": "DEBIT",
    "currencyMode": "SINGLE",
    "currencyCode": "USD",
    "isActive": true
  }'
```

### 5. Create Journal Entries
```bash
curl -X POST "http://localhost:8080/api/v1/ledgers/456e7890-e89b-12d3-a456-426614174000/journal-entries" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "accountingDate": "2025-01-15",
    "description": "Office supplies purchase",
    "journalLines": [...]
  }'
```

## Update Operations

### **Partial Updates**
All update APIs support partial updates - you only need to include the fields you want to change:

```bash
# Update only the account name
PUT /api/v1/accounts/{accountId}
{
  "name": "New Account Name"
}

# Update only the party contact details
PUT /api/v1/parties/{partyId}
{
  "contactDetails": "{\"email\": \"newemail@example.com\"}"
}
```

### **Validation Rules**
- **Account Updates**: Cannot change account type or normal side if the account has existing transactions
- **Ledger Updates**: Functional currency changes may require revaluation of existing balances
- **Party Updates**: Type changes may affect existing journal entries
- **Tenant Updates**: Name changes are immediately reflected across all related entities

## Benefits

1. **Clean API**: No need to pass tenant ID to every service method
2. **Automatic**: Tenant context is set automatically from HTTP headers
3. **Thread-safe**: Uses ThreadLocal for proper isolation
4. **Memory-safe**: Context is automatically cleared after each request
5. **Consistent**: All services use the same tenant context mechanism
6. **Complete**: Full CRUD operations for tenants, ledgers, parties, accounts, and journal entries
7. **Flexible**: Partial updates supported for all entities
8. **Secure**: Tenant ownership verification on all update operations

## Security Considerations

- Always validate that the tenant ID in the context matches the expected tenant for the operation
- The filter automatically clears the context after each request to prevent memory leaks
- Update operations verify tenant ownership before allowing modifications
- Consider adding additional security checks in production (e.g., JWT validation, role-based access)

## Error Handling

If a service is called without a tenant context set, it will throw an `IllegalStateException`:

```java
if (currentTenantId == null) {
    throw new IllegalStateException("Tenant context not set");
}
```

This ensures that all operations are properly scoped to a tenant.
