# General Ledger Application

A comprehensive, multi-currency, double-entry general ledger system built with Spring Boot and PostgreSQL.

## Overview

This application implements a production-ready general ledger system with the following key features:

- **Double-entry accounting**: Every journal entry balances to zero in the functional currency
- **Multi-currency support**: Full FX rate management and currency conversion
- **Multi-tenant architecture**: Complete data isolation between tenants
- **Audit trail**: Immutable posting records and comprehensive audit logging
- **Performance optimized**: Balance snapshots for fast reporting and aggregation

## Architecture

### Core Entities

- **Tenant**: Multi-tenant boundary with isolated data
- **Ledger**: Book of accounts with functional currency and timezone
- **Account**: Chart of accounts with hierarchical structure
- **JournalEntry**: Financial transactions composed of journal lines
- **JournalLine**: Individual debit/credit entries with currency and FX rates
- **Posting**: Immutable atomic postings for fast aggregation
- **Currency**: Currency definitions with rounding rules
- **FxRate**: Exchange rates between currency pairs
- **BalanceSnapshot**: Pre-aggregated balances for performance
- **Period**: Accounting periods with status management
- **Party**: External entities (customers, vendors, employees)

### Key Design Principles

1. **Append-only postings**: Once posted, entries cannot be modified
2. **Functional currency balancing**: All entries must balance in the ledger's functional currency
3. **FX rate management**: Comprehensive exchange rate handling with date-based lookups
4. **Performance optimization**: Balance snapshots for fast reporting
5. **Data integrity**: Comprehensive validation and constraint enforcement

## Database Schema

The database schema is managed through Liquibase and includes:

- **Proper relationships**: All entities use proper JPA relationships instead of raw UUIDs
- **Multi-currency support**: FX rates, currency constraints, and conversion logic
- **Performance indexes**: Optimized for common query patterns
- **Data integrity**: Foreign key constraints and unique constraints
- **Audit fields**: Created/updated timestamps on all entities

### Key Schema Features

- **Currency table**: Supports currencies with different decimal places (e.g., JPY has 0 decimals)
- **FX rates**: Date-based exchange rates with source tracking
- **Balance snapshots**: Pre-calculated balances for fast reporting
- **Proper indexing**: Tenant-aware indexes for multi-tenant performance

## API Endpoints

### Journal Entries
- `POST /api/ledgers/{ledgerId}/journal-entries` - Create journal entry
- `POST /api/journal-entries/{id}/post` - Post a draft entry
- `GET /api/journal-entries/{id}` - Get journal entry by ID

### FX Rates
- `POST /api/fx/rates` - Create/update exchange rate
- `GET /api/fx/rates` - Get most recent rate for currency pair
- `GET /api/fx/rates/{baseCode}/{quoteCode}/{asOf}` - Get rate for specific date
- `GET /api/fx/rates/convert` - Convert amount between currencies

### Accounts
- `POST /api/ledgers/{ledgerId}/accounts` - Create account
- `GET /api/ledgers/{ledgerId}/accounts` - List accounts for ledger
- `PATCH /api/accounts/{id}` - Update account

### Ledgers
- `POST /api/ledgers` - Create ledger
- `GET /api/ledgers/{id}` - Get ledger by ID
- `GET /api/ledgers` - List ledgers for tenant

### Periods
- `POST /api/periods` - Create new period
- `GET /api/periods/ledger/{ledgerId}` - Get periods for ledger
- `GET /api/periods/ledger/{ledgerId}/status/{status}` - Get periods by status
- `GET /api/periods/ledger/{ledgerId}/open` - Get open period for ledger
- `POST /api/periods/{id}/close` - Close period
- `POST /api/periods/{id}/lock` - Lock period
- `POST /api/periods/{id}/reopen` - Reopen period
- `GET /api/periods/{id}` - Get period by ID

## Business Logic Implementation

### Journal Entry Posting

The `postJournalEntry` method implements comprehensive business logic:

1. **Validation**:
   - Account existence and tenant ownership
   - Account active status
   - Currency constraints (single vs. multi-currency accounts)
   - Period status (only OPEN periods allow posting)

2. **FX Conversion**:
   - Automatic conversion to functional currency
   - FX rate lookup with fallback to inverse rates
   - Precision handling with BigDecimal arithmetic

3. **Balancing**:
   - Functional currency balance validation
   - Debit/credit equality enforcement
   - Error handling for unbalanced entries

4. **Posting Creation**:
   - Immutable posting records
   - Proper relationship mapping
   - Sequence number assignment

### FX Rate Management

- **Rate storage**: Date-based exchange rates with source tracking
- **Rate lookup**: Most recent rate as of a specific date
- **Conversion logic**: Direct and inverse rate handling
- **Precision**: BigDecimal arithmetic with proper rounding

### Balance Management

- **Snapshot creation**: Pre-calculated balances for performance
- **Running balances**: Incremental balance calculations
- **Multi-currency**: Native and functional currency balances

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 12+
- Gradle 7+

### Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd general-ledger
   ```

2. **Configure database**:
   - Create PostgreSQL database
   - Update `application.yml` with database credentials

3. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

4. **Initialize data**:
   - The application will automatically create the schema via Liquibase
   - Sample currencies (USD, EUR, GBP, etc.) are pre-loaded

### Configuration

Key configuration options in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/general_ledger
    username: your_username
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Development

### Project Structure

```
src/main/java/com/cxm360/ai/ledger/
├── config/          # Configuration classes
├── context/         # Tenant context management
├── controller/      # REST API controllers
├── dto/            # Data transfer objects
├── filter/         # HTTP filters
├── mapper/         # Entity-DTO mappers
├── model/          # JPA entities
├── repository/     # Data access layer
└── service/        # Business logic layer
```

### Key Components

- **TenantContext**: Thread-local tenant context for multi-tenancy
- **TenantContextFilter**: HTTP filter for tenant context injection
- **JsonNodeConverter**: JSON handling for flexible settings storage
- **Validation**: Comprehensive business rule validation

### Testing

Run tests with:
```bash
./gradlew test
```

The test suite includes:
- Unit tests for business logic
- Integration tests with PostgreSQL
- Multi-tenant context testing

## Recent Improvements

### Fixed Issues

1. **Database Schema Alignment**: 
   - Corrected schema to match design document
   - Added missing tables (Currency, FxRate, BalanceSnapshot)
   - Fixed column types and relationships

2. **Entity Relationships**:
   - Replaced raw UUIDs with proper JPA relationships
   - Added missing entity classes
   - Implemented bidirectional relationship management

3. **Business Logic**:
   - Complete journal entry posting implementation
   - FX rate management and currency conversion
   - Comprehensive validation and error handling

4. **Performance**:
   - Added proper database indexes
   - Implemented balance snapshot strategy
   - Optimized query patterns

### New Features

- **FX Rate Management**: Complete exchange rate handling
- **Balance Snapshots**: Pre-calculated balances for reporting
- **Currency Support**: Multi-currency account management
- **Period Management**: Accounting period status control
- **Comprehensive Validation**: Business rule enforcement

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues, please:
1. Check the documentation
2. Review existing issues
3. Create a new issue with detailed information
