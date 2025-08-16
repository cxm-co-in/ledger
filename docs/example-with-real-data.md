# General Ledger - Complete Example with Real Data

## **Scenario: TechCorp Inc. (Multi-tenant, Multi-currency)**

### **Tenant Setup**
```sql
-- Tenant
INSERT INTO tenant (id, name, settings) VALUES (
  '550e8400-e29b-41d4-a716-446655440001',
  'TechCorp Inc.',
  '{"timezone": "America/New_York", "fiscal_year_start": "01-01"}'
);

-- Ledger (USD functional currency)
INSERT INTO ledger (id, tenant_id, name, functional_currency_code, timezone, settings) VALUES (
  '550e8400-e29b-41d4-a716-446655440002',
  '550e8400-e29b-41d4-a716-446655440001',
  'Main Ledger',
  'USD',
  'America/New_York',
  '{"revaluation_method": "PERIOD_END", "rounding_account": "9999-ROUNDING"}'
);
```

### **Chart of Accounts**
```sql
-- Root Accounts
INSERT INTO account (id, tenant_id, ledger_id, code, name, type, normal_side, currency_mode, currency_code, is_active, parent_account_id) VALUES
-- Assets
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1000', 'Assets', 'ASSET', 'DEBIT', 'MULTI', NULL, true, NULL),
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1100', 'Cash & Bank', 'ASSET', 'DEBIT', 'MULTI', NULL, true, '550e8400-e29b-41d4-a716-446655440010'),
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1101', 'Bank of America USD', 'ASSET', 'DEBIT', 'SINGLE', 'USD', true, '550e8400-e29b-41d4-a716-446655440011'),
('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1102', 'Deutsche Bank EUR', 'ASSET', 'DEBIT', 'SINGLE', 'EUR', true, '550e8400-e29b-41d4-a716-446655440011'),
('550e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1200', 'Accounts Receivable', 'ASSET', 'DEBIT', 'MULTI', NULL, true, '550e8400-e29b-41d4-a716-446655440010'),
('550e8400-e29b-41d4-a716-446655440015', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1201', 'AR - US Customers', 'ASSET', 'DEBIT', 'SINGLE', 'USD', true, '550e8400-e29b-41d4-a716-446655440014'),
('550e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '1202', 'AR - EU Customers', 'ASSET', 'DEBIT', 'SINGLE', 'EUR', true, '550e8400-e29b-41d4-a716-446655440014'),

-- Liabilities
('550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2000', 'Liabilities', 'LIABILITY', 'CREDIT', 'MULTI', NULL, true, NULL),
('550e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2100', 'Accounts Payable', 'LIABILITY', 'CREDIT', 'MULTI', NULL, true, '550e8400-e29b-41d4-a716-446655440020'),
('550e8400-e29b-41d4-a716-446655440022', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2101', 'AP - US Vendors', 'LIABILITY', 'CREDIT', 'SINGLE', 'USD', true, '550e8400-e29b-41d4-a716-446655440021'),
('550e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2102', 'AP - EU Vendors', 'LIABILITY', 'CREDIT', 'SINGLE', 'EUR', true, '550e8400-e29b-41d4-a716-446655440021'),

-- Equity
('550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '3000', 'Equity', 'EQUITY', 'CREDIT', 'MULTI', NULL, true, NULL),
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '3100', 'Common Stock', 'EQUITY', 'CREDIT', 'SINGLE', 'USD', true, '550e8400-e29b-41d4-a716-446655440030'),
('550e8400-e29b-41d4-a716-446655440032', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '3200', 'Retained Earnings', 'EQUITY', 'CREDIT', 'SINGLE', 'USD', true, '550e8400-e29b-41d4-a716-446655440030'),

-- Revenue
('550e8400-e29b-41d4-a716-446655440040', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '4000', 'Revenue', 'REVENUE', 'CREDIT', 'MULTI', NULL, true, NULL),
('550e8400-e29b-41d4-a716-446655440041', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '4100', 'Software License Revenue', 'REVENUE', 'CREDIT', 'MULTI', NULL, true, '550e8400-e29b-41d4-a716-446655440040'),

-- Expenses
('550e8400-e29b-41d4-a716-446655440050', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '5000', 'Expenses', 'EXPENSE', 'DEBIT', 'MULTI', NULL, true, NULL),
('550e8400-e29b-41d4-a716-446655440051', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '5100', 'Cost of Goods Sold', 'EXPENSE', 'DEBIT', 'MULTI', NULL, true, '550e8400-e29b-41d4-a716-446655440050'),
('550e8400-e29b-41d4-a716-446655440052', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '5200', 'Operating Expenses', 'EXPENSE', 'DEBIT', 'MULTI', NULL, true, '550e8400-e29b-41d4-a716-446655440050'),
('550e8400-e29b-41d4-a716-446655440053', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '5201', 'FX Gain/Loss', 'EXPENSE', 'DEBIT', 'SINGLE', 'USD', true, '550e8400-e29b-41d4-a716-446655440052'),

-- Rounding Account
('550e8400-e29b-41d4-a716-446655440099', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '9999', 'Rounding Differences', 'CONTRA', 'DEBIT', 'SINGLE', 'USD', true, NULL);
```

### **Currencies**
```sql
INSERT INTO currency (code, name, exponent, rounding_mode, cash_rounding_increment, is_obsolete) VALUES
('USD', 'US Dollar', 2, 'HALF_EVEN', 0.01, false),
('EUR', 'Euro', 2, 'HALF_EVEN', 0.01, false);
```

### **FX Rates**
```sql
INSERT INTO fx_rate (base_code, quote_code, as_of, rate, source, inserted_at) VALUES
('EUR', 'USD', '2025-01-15', 1.0850, 'ECB', '2025-01-15 09:00:00'),
('EUR', 'USD', '2025-01-20', 1.0920, 'ECB', '2025-01-20 09:00:00'),
('EUR', 'USD', '2025-01-25', 1.0880, 'ECB', '2025-01-25 09:00:00');
```

### **Periods**
```sql
INSERT INTO period (id, tenant_id, ledger_id, start_date, end_date, status) VALUES
('550e8400-e29b-41d4-a716-446655440060', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2025-01-01', '2025-01-31', 'OPEN');
```

## **Example 1: Software License Sale (Multi-currency)**

### **Journal Entry: Sale to German Customer**
```sql
-- Journal Entry
INSERT INTO journal_entry (id, tenant_id, ledger_id, accounting_date, transaction_date, status, sequence_no, external_id, idempotency_key, description, posted_at, metadata) VALUES (
  '550e8400-e29b-41d4-a716-446655440100',
  '550e8400-e29b-41d4-a716-446655440001',
  '550e8400-e29b-41d4-a716-446655440002',
  '2025-01-15',
  '2025-01-15',
  'POSTED',
  1001,
  'INV-2025-001',
  'sale-de-001-2025-01-15',
  'Software license sale to German customer',
  '2025-01-15 10:30:00',
  '{"customer_id": "DE-CUST-001", "invoice_number": "INV-2025-001"}'
);

-- Journal Lines
INSERT INTO journal_line (id, tenant_id, entry_id, account_id, direction, currency_code, amount_minor, fx_rate, functional_amount_minor, memo, dimensions) VALUES
-- Revenue line (EUR 10,000)
('550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440041', 'CREDIT', 'EUR', 1000000, 1.0850, 1085000, 'Software license revenue', '{"product": "Enterprise Suite", "region": "EU"}'),
-- AR line (EUR 10,000)
('550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440016', 'DEBIT', 'EUR', 1000000, 1.0850, 1085000, 'Accounts receivable', '{"customer": "DE-CUST-001", "terms": "Net 30"}');

-- Postings (Atomic events)
INSERT INTO posting (id, tenant_id, ledger_id, entry_id, line_id, account_id, accounting_date, currency_code, amount_minor_signed, posted_at) VALUES
-- Revenue posting (CREDIT = negative amount)
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440041', '2025-01-15', 'EUR', -1000000, '2025-01-15 10:30:00'),
-- AR posting (DEBIT = positive amount)
('550e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440016', '2025-01-15', 'EUR', 1000000, '2025-01-15 10:30:00');
```

## **Example 2: Payment Received (FX Gain)**

### **Journal Entry: Customer Payment**
```sql
-- Journal Entry
INSERT INTO journal_entry (id, tenant_id, ledger_id, accounting_date, transaction_date, status, sequence_no, external_id, idempotency_key, description, posted_at, metadata) VALUES (
  '550e8400-e29b-41d4-a716-446655440110',
  '550e8400-e29b-41d4-a716-446655440001',
  '550e8400-e29b-41d4-a716-446655440002',
  '2025-01-20',
  '2025-01-20',
  'POSTED',
  1002,
  'PAY-2025-001',
  'payment-de-001-2025-01-20',
  'Payment received from German customer',
  '2025-01-20 14:15:00',
  '{"customer_id": "DE-CUST-001", "payment_method": "Bank Transfer"}'
);

-- Journal Lines
INSERT INTO journal_line (id, tenant_id, entry_id, account_id, direction, currency_code, amount_minor, fx_rate, functional_amount_minor, memo, dimensions) VALUES
-- Cash receipt (EUR 10,000 at current rate 1.0920)
('550e8400-e29b-41d4-a716-446655440111', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440013', 'DEBIT', 'EUR', 1000000, 1.0920, 1092000, 'Cash received', '{"bank": "Deutsche Bank", "reference": "TRX-001"}'),
-- AR reduction (EUR 10,000 at historical rate 1.0850)
('550e8400-e29b-41d4-a716-446655440112', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440016', 'CREDIT', 'EUR', 1000000, 1.0850, 1085000, 'Accounts receivable reduction', '{"customer": "DE-CUST-001"}'),
-- FX Gain (USD 70 = 1092000 - 1085000)
('550e8400-e29b-41d4-a716-446655440113', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440053', 'CREDIT', 'USD', 7000, 1.0000, 7000, 'FX gain on EUR collection', '{"fx_type": "realized", "base_currency": "EUR"}');

-- Postings
INSERT INTO posting (id, tenant_id, ledger_id, entry_id, line_id, account_id, accounting_date, currency_code, amount_minor_signed, posted_at) VALUES
-- Cash posting (DEBIT = positive)
('550e8400-e29b-41d4-a716-446655440211', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440111', '550e8400-e29b-41d4-a716-446655440013', '2025-01-20', 'EUR', 1000000, '2025-01-20 14:15:00'),
-- AR posting (CREDIT = negative)
('550e8400-e29b-41d4-a716-446655440212', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440112', '550e8400-e29b-41d4-a716-446655440016', '2025-01-20', 'EUR', -1000000, '2025-01-20 14:15:00'),
-- FX Gain posting (CREDIT = negative)
('550e8400-e29b-41d4-a716-446655440213', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440113', '550e8400-e29b-41d4-a716-446655440053', '2025-01-20', 'USD', -7000, '2025-01-20 14:15:00');
```

## **Example 3: Balance Snapshot (As of 2025-01-25)**

```sql
INSERT INTO balance_snapshot (tenant_id, ledger_id, account_id, currency_code, as_of_date, debit_minor, credit_minor, balance_minor, entry_count) VALUES
-- Cash accounts
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440012', 'USD', '2025-01-25', 0, 0, 0, 0),
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440013', 'EUR', '2025-01-25', 1000000, 0, 1000000, 1),

-- AR accounts
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440015', 'USD', '2025-01-25', 0, 0, 0, 0),
('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440016', 'EUR', '2025-01-25', 0, 0, 0, 0),

-- Revenue accounts
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440041', 'EUR', '2025-01-25', 0, 1000000, -1000000, 1),

-- FX Gain/Loss
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440053', 'USD', '2025-01-25', 0, 7000, -7000, 1);
```

## **Key Insights from the Examples**

### **1. Why POSTING is Essential**
- **Audit Trail**: Every change to every account is recorded
- **Performance**: Fast balance calculations without complex joins
- **Flexibility**: One journal line can generate multiple postings
- **Compliance**: Complete transaction history for regulators

### **2. Multi-Currency Handling**
- **Functional Currency**: All entries balance in USD (functional)
- **Line Currency**: Individual lines can be in EUR
- **FX Rates**: Stored per transaction for audit
- **FX Gains**: Automatically calculated on settlement

### **3. Balance Calculation**
- **USD Bank**: 0 (no transactions)
- **EUR Bank**: €10,000 (1,000,000 minor units)
- **EUR AR**: €0 (fully collected)
- **EUR Revenue**: -€10,000 (credit balance)
- **USD FX Gain**: -$70 (credit balance)

### **4. Data Relationships**
```
JOURNAL_ENTRY (1) → JOURNAL_LINE (N) → POSTING (N)
                    ↓
                ACCOUNT (1)
                    ↓
            BALANCE_SNAPSHOT (N)
```

### **5. Business Value**
- **Real-time Balances**: Fast account statements
- **Multi-currency Reports**: Both native and functional currency views
- **FX Analysis**: Track realized/unrealized gains/losses
- **Audit Compliance**: Complete transaction trail
- **Performance**: Efficient queries for large datasets

The `POSTING` entity transforms your ledger from a simple journal into a powerful, auditable, and performant financial system!
