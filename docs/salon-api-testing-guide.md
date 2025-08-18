# Salon Business API Testing Guide

## **Overview**

This guide provides comprehensive examples for testing your General Ledger APIs using the **Glamour Salon & Spa** sample data. The sample data includes:

- **Tenant**: Glamour Salon & Spa
- **Ledger**: Main Ledger (USD)
- **Period**: Q1 2025 (Jan 1 - Mar 31, 2025)
- **Parties**: Customers, Vendors, Employees
- **Accounts**: Complete Chart of Accounts for salon business

## **Sample Data IDs**

### **Core Entities**
- **Tenant ID**: `550e8400-e29b-41d4-a716-446655440100`
- **Ledger ID**: `550e8400-e29b-41d4-a716-446655440101`
- **Period ID**: `550e8400-e29b-41d4-a716-446655440160`

### **Parties**
- **Sarah Johnson (Customer)**: `550e8400-e29b-41d4-a716-446655440170`
- **Mike Chen (Customer)**: `550e8400-e29b-41d4-a716-446655440171`
- **Professional Beauty Supply Co. (Vendor)**: `550e8400-e29b-41d4-a716-446655440172`
- **Emma Rodriguez (Employee)**: `550e8400-e29b-41d4-a716-446655440173`

### **Key Accounts**
- **Cash**: `550e8400-e29b-41d4-a716-446655440182`
- **Hair Services Revenue**: `550e8400-e29b-41d4-a716-446655440192`
- **Hair Products Revenue**: `550e8400-e29b-41d4-a716-446655440195`
- **Sales Tax Payable**: `550e8400-e29b-41d4-a716-446655440199`

## **API Testing Examples**

### **1. Get Tenant Information**

```bash
curl -X GET "http://localhost:8080/api/v1/tenants/550e8400-e29b-41d4-a716-446655440100" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100"
```

**Expected Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440100",
  "name": "Glamour Salon & Spa",
  "settings": {
    "timezone": "America/New_York",
    "fiscal_year_start": "01-01",
    "business_type": "salon",
    "tax_rate": 8.5,
    "currency": "USD"
  }
}
```

### **2. Get Ledger Information**

```bash
curl -X GET "http://localhost:8080/api/v1/ledgers/550e8400-e29b-41d4-a716-446655440101" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100"
```

**Expected Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440101",
  "tenantId": "550e8400-e29b-41d4-a716-446655440100",
  "name": "Main Ledger",
  "functionalCurrencyCode": "USD",
  "timezone": "America/New_York",
  "settings": {
    "revaluation_method": "PERIOD_END",
    "rounding_account": "9999-ROUNDING"
  }
}
```

### **3. Get Chart of Accounts**

```bash
curl -X GET "http://localhost:8080/api/v1/ledgers/550e8400-e29b-41d4-a716-446655440101/accounts" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100"
```

**Expected Response** (partial):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440180",
    "code": "1000",
    "name": "Assets",
    "type": "ASSET",
    "normalSide": "DEBIT",
    "isActive": true,
    "parentAccountId": null
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440181",
    "code": "1100",
    "name": "Current Assets",
    "type": "ASSET",
    "normalSide": "DEBIT",
    "isActive": true,
    "parentAccountId": "550e8400-e29b-41d4-a716-446655440180"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440192",
    "code": "4101",
    "name": "Hair Services",
    "type": "REVENUE",
    "normalSide": "CREDIT",
    "isActive": true,
    "parentAccountId": "550e8400-e29b-41d4-a716-446655440191"
  }
]
```

### **4. Get Parties (Customers, Vendors, Employees)**

```bash
curl -X GET "http://localhost:8080/api/v1/parties" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100"
```

**Expected Response** (partial):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440170",
    "name": "Sarah Johnson",
    "type": "CUSTOMER",
    "externalId": "CUST-001",
    "contactDetails": {
      "email": "sarah.j@email.com",
      "phone": "555-0101",
      "address": "123 Main St, Anytown, NY"
    }
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440172",
    "name": "Professional Beauty Supply Co.",
    "type": "VENDOR",
    "externalId": "VEND-001",
    "contactDetails": {
      "email": "orders@probeauty.com",
      "phone": "800-555-0123",
      "address": "789 Supply Blvd, Beauty City, CA"
    }
  }
]
```

### **5. Create a Journal Entry (Hair Service + Product Sale)**

```bash
curl -X POST "http://localhost:8080/api/v1/journal-entries" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100" \
  -H "Content-Type: application/json" \
  -d '{
    "ledgerId": "550e8400-e29b-41d4-a716-446655440101",
    "accountingDate": "2025-01-15",
    "transactionDate": "2025-01-15",
    "description": "Hair cut service + styling product sale for Sarah Johnson",
    "externalId": "INV-2025-001",
    "idempotencyKey": "hair-service-sarah-001-2025-01-15",
    "lines": [
      {
        "accountId": "550e8400-e29b-41d4-a716-446655440192",
        "partyId": "550e8400-e29b-41d4-a716-446655440170",
        "direction": "CREDIT",
        "currencyCode": "USD",
        "amountMinor": 4500,
        "memo": "Hair cut and styling service",
        "dimensions": {
          "service": "hair_cut",
          "stylist": "Emma Rodriguez",
          "customer": "Sarah Johnson"
        }
      },
      {
        "accountId": "550e8400-e29b-41d4-a716-446655440195",
        "partyId": "550e8400-e29b-41d4-a716-446655440170",
        "direction": "CREDIT",
        "currencyCode": "USD",
        "amountMinor": 2500,
        "memo": "Styling product sale",
        "dimensions": {
          "product": "hair_spray",
          "category": "styling",
          "customer": "Sarah Johnson"
        }
      },
      {
        "accountId": "550e8400-e29b-41d4-a716-446655440199",
        "direction": "CREDIT",
        "currencyCode": "USD",
        "amountMinor": 595,
        "memo": "Sales tax on service and product",
        "dimensions": {
          "tax_rate": "8.5%",
          "tax_type": "sales_tax"
        }
      },
      {
        "accountId": "550e8400-e29b-41d4-a716-446655440182",
        "direction": "DEBIT",
        "currencyCode": "USD",
        "amountMinor": 7595,
        "memo": "Cash received for service and product",
        "dimensions": {
          "payment_method": "cash",
          "customer": "Sarah Johnson"
        }
      }
    ]
  }'
```

**Expected Response**:
```json
{
  "id": "generated-uuid",
  "tenantId": "550e8400-e29b-41d4-a716-446655440100",
  "ledgerId": "550e8400-e29b-41d4-a716-446655440101",
  "accountingDate": "2025-01-15",
  "status": "POSTED",
  "description": "Hair cut service + styling product sale for Sarah Johnson",
  "lines": [
    {
      "id": "generated-uuid",
      "accountId": "550e8400-e29b-41d4-a716-446655440192",
      "direction": "CREDIT",
      "amountMinor": 4500,
      "memo": "Hair cut and styling service"
    }
    // ... other lines
  ]
}
```

### **6. Get Account Balance**

```bash
curl -X GET "http://localhost:8080/api/v1/accounts/550e8400-e29b-41d4-a716-446655440192/balance" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100" \
  -H "Content-Type: application/json" \
  -d '{
    "asOfDate": "2025-01-15",
    "currencyCode": "USD"
  }'
```

**Expected Response**:
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440192",
  "accountName": "Hair Services",
  "asOfDate": "2025-01-15",
  "currencyCode": "USD",
  "debitMinor": 0,
  "creditMinor": 4500,
  "balanceMinor": -4500,
  "balanceType": "CREDIT"
}
```

### **7. Create a New Customer**

```bash
curl -X POST "http://localhost:8080/api/v1/parties" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lisa Thompson",
    "type": "CUSTOMER",
    "externalId": "CUST-003",
    "contactDetails": {
      "email": "lisa.thompson@email.com",
      "phone": "555-0103",
      "address": "789 Pine St, Anytown, NY"
    }
  }'
```

### **8. Create a New Account**

```bash
curl -X POST "http://localhost:8080/api/v1/accounts" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100" \
  -H "Content-Type: application/json" \
  -d '{
    "ledgerId": "550e8400-e29b-41d4-a716-446655440101",
    "code": "4103",
    "name": "Spa Services",
    "type": "REVENUE",
    "normalSide": "CREDIT",
    "currencyMode": "SINGLE",
    "currencyCode": "USD",
    "isActive": true,
    "parentAccountId": "550e8400-e29b-41d4-a716-446655440191"
  }'
```

## **Business Scenarios to Test**

### **Scenario 1: Daily Salon Operations**
1. **Create customer** (if new)
2. **Record service** (hair cut, nail service, etc.)
3. **Sell product** (shampoo, styling products)
4. **Calculate tax** (8.5% in this example)
5. **Record payment** (cash, card, etc.)

### **Scenario 2: Inventory Management**
1. **Purchase inventory** from vendor
2. **Record cost** in inventory account
3. **Sell products** to customers
4. **Track COGS** when products are sold

### **Scenario 3: Employee Payroll**
1. **Record employee time** and services
2. **Calculate commissions** based on sales
3. **Record payroll expenses**

### **Scenario 4: Financial Reporting**
1. **Get account balances** for specific dates
2. **Generate P&L** by service category
3. **Track cash flow** from operations

## **Testing Tips**

### **1. Use Consistent Tenant ID**
Always include `X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440100` in headers

### **2. Test Data Integrity**
- Verify journal entries balance (debits = credits)
- Check account balances after transactions
- Validate foreign key relationships

### **3. Test Business Rules**
- Ensure revenue accounts have credit balances
- Verify asset accounts have debit balances
- Check that tax calculations are correct

### **4. Monitor Database**
```sql
-- Check what Liquibase has applied
SELECT * FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED;

-- Verify sample data exists
SELECT COUNT(*) FROM tenant WHERE name = 'Glamour Salon & Spa';
SELECT COUNT(*) FROM account WHERE ledger_id = '550e8400-e29b-41d4-a716-446655440101';
```

## **Next Steps**

1. **Start your application** - Liquibase will automatically create the sample data
2. **Test the APIs** using the examples above
3. **Create real business transactions** for your salon
4. **Extend the chart of accounts** as needed
5. **Add more business logic** specific to salon operations

This sample data gives you a **complete, realistic salon business** to test all your General Ledger APIs! 🚀
