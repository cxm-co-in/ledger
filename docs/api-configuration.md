# API Configuration Guide

## Overview

The General Ledger application now supports configurable API base paths instead of hardcoded values. This allows for easier deployment customization and environment-specific configurations.

## Configuration Properties

### All API Controllers
All controllers now use the same base path configuration for consistency:

**Property:** `api.base-path`
**Default:** `/api/v1`

**Controllers using this configuration:**
- `AccountController` → `${api.base-path}` → `/api/v1`
- `LedgerController` → `${api.base-path}` → `/api/v1`
- `JournalEntryController` → `${api.base-path}` → `/api/v1`
- `PartyController` → `${api.base-path}` → `/api/v1`
- `TenantController` → `${api.base-path}` → `/api/v1`
- `FxRateController` → `${api.base-path}/fx/rates` → `/api/v1/fx/rates`
- `PeriodController` → `${api.base-path}/periods` → `/api/v1/periods`

## Example Configuration

```yaml
# application.yml
api:
  base-path: /api/v1
```

## Environment-Specific Configuration

You can override this value in different environment profiles:

### Development Environment
```yaml
# application-dev.yml
api:
  base-path: /api/v1
```

### Production Environment
```yaml
# application-prod.yml
api:
  base-path: /api/v1
```

### Custom Deployment
```yaml
# application-custom.yml
api:
  base-path: /ledger/api/v1
```

## API Endpoint Structure

With the default configuration (`/api/v1`), your API endpoints will be:

### Main Business Controllers
- **Accounts**: `/api/v1/accounts`, `/api/v1/ledgers/{ledgerId}/accounts`
- **Ledgers**: `/api/v1/ledgers`, `/api/v1/ledgers/{ledgerId}`
- **Journal Entries**: `/api/v1/ledgers/{ledgerId}/journal-entries`
- **Parties**: `/api/v1/parties`, `/api/v1/parties/{partyId}`
- **Tenants**: `/api/v1/tenants`, `/api/v1/tenants/{tenantId}`

### Specialized Controllers
- **FX Rates**: `/api/v1/fx/rates`, `/api/v1/fx/rates/convert`
- **Periods**: `/api/v1/periods`, `/api/v1/periods/ledger/{ledgerId}`

## Benefits

1. **Consistency**: All controllers use the same base configuration
2. **Flexibility**: Easy to change API paths without code changes
3. **Environment Isolation**: Different paths for different environments
4. **Deployment Customization**: Support for custom API path structures
5. **Maintainability**: Centralized configuration management
6. **Logical Organization**: Related functionality grouped under meaningful sub-paths

## Migration Notes

- All controllers now use `${api.base-path}` instead of hardcoded paths
- FX and Period controllers use `${api.base-path}/fx/rates` and `${api.base-path}/periods` respectively
- Default values maintain backward compatibility
- No breaking changes to existing API consumers
- Simplified configuration with just one property to manage
