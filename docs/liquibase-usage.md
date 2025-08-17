# Liquibase Database Management

This document explains how to use Liquibase for database schema management in the General Ledger system.

## 🚀 What is Liquibase?

Liquibase is a database schema migration tool that:
- **Tracks database changes** in version control
- **Automatically applies schema updates** when the application starts
- **Supports rollbacks** to previous database states
- **Works with multiple databases** (PostgreSQL, MySQL, Oracle, etc.)
- **Integrates seamlessly** with Spring Boot

## 📁 Project Structure

```
src/main/resources/db/
├── changelog/
│   ├── db.changelog-master.xml          # Master changelog file
│   └── changes/                         # Individual change files
│       ├── 001-initial-schema.xml       # Creates all tables
│       └── 002-sample-data.xml          # Inserts sample data
```

## 🛠️ Available Commands

### **Check Database Status**
```bash
make liquibase-status
```
Shows which changes have been applied and which are pending.

### **Apply Database Changes**
```bash
make liquibase-update
```
Applies all pending database changes.

### **Rollback Changes**
```bash
make liquibase-rollback
```
Rolls back the last applied change (interactive prompt for count).

## 🔄 How It Works

### **1. Automatic Execution**
- Liquibase runs automatically when the application starts
- It checks the `DATABASECHANGELOG` table to see what's been applied
- Applies any new changes found in the changelog files

### **2. Change Tracking**
- Each change is tracked with a unique ID and author
- Changes are applied in order based on the master changelog
- Once applied, changes cannot be modified (only new changes can be added)

### **3. Rollback Support**
- Liquibase can rollback changes if needed
- Rollbacks are useful for development and testing
- Production rollbacks should be carefully planned

## 📝 Creating New Changes

### **1. Create a New Changelog File**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="003-add-new-table" author="your-name">
        <comment>Add new table for feature X</comment>
        
        <createTable tableName="new_table">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="name" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
        </createTable>
        
    </changeSet>

</databaseChangeLog>
```

### **2. Include in Master Changelog**
```xml
<!-- In db.changelog-master.xml -->
<include file="db/changelog/changes/003-add-new-table.xml"/>
```

### **3. Apply Changes**
```bash
make liquibase-update
```

## 🗄️ Database Schema

The current schema includes:

### **Core Tables**
- **`tenant`** - Multi-tenant organization
- **`ledger`** - Chart of accounts container
- **`period`** - Accounting periods (quarters, years)
- **`account`** - Chart of accounts
- **`party`** - Customers, vendors, employees
- **`journal_entry`** - Journal entries
- **`journal_line`** - Individual debit/credit lines
- **`posting`** - Posted transactions

### **Key Features**
- **UUID primary keys** for scalability
- **JSONB fields** for flexible settings and metadata
- **Proper foreign keys** for referential integrity
- **Performance indexes** on frequently queried columns
- **Audit fields** (created_at, updated_at)

## 🔧 Configuration

### **Application Properties**
```properties
# Liquibase Configuration
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.contexts=dev
spring.liquibase.default-schema=public
```

### **Gradle Plugin**
```gradle
plugins {
    id 'org.liquibase.gradle' version '2.2.1'
}
```

## 🚨 Best Practices

### **1. ChangeSet Guidelines**
- Use **unique, descriptive IDs** (e.g., `001-initial-schema`)
- Include **meaningful comments** explaining the change
- **One logical change per changeSet**
- Use **consistent naming conventions**

### **2. Development Workflow**
- **Test changes locally** before committing
- **Use descriptive branch names** for feature development
- **Review changes** before merging to main
- **Document complex changes** in commit messages

### **3. Production Considerations**
- **Never modify applied changes** in production
- **Test rollbacks** in staging environments
- **Backup database** before major schema changes
- **Monitor change application** in production logs

## 🐛 Troubleshooting

### **Common Issues**

#### **1. Change Already Applied**
```
Error: ChangeSet already applied
```
**Solution**: Check the `DATABASECHANGELOG` table to see what's been applied.

#### **2. Foreign Key Violations**
```
Error: Foreign key constraint violation
```
**Solution**: Ensure dependent tables are created before referencing tables.

#### **3. Duplicate ChangeSet IDs**
```
Error: Duplicate changeSet ID
```
**Solution**: Use unique IDs across all changelog files.

### **Useful Commands**

#### **Check Database State**
```sql
-- See applied changes
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED;

-- See change details
SELECT * FROM DATABASECHANGELOG WHERE ID = 'your-change-id';
```

#### **Manual Rollback (if needed)**
```sql
-- Remove specific change from tracking
DELETE FROM DATABASECHANGELOG WHERE ID = 'your-change-id';

-- Drop tables manually (be careful!)
DROP TABLE IF EXISTS table_name CASCADE;
```

## 📚 Additional Resources

- [Liquibase Documentation](https://docs.liquibase.com/)
- [Spring Boot + Liquibase](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [PostgreSQL JSONB](https://www.postgresql.org/docs/current/datatype-json.html)

## 🎯 Next Steps

1. **Review the current schema** in `001-initial-schema.xml`
2. **Test the setup** by running `make liquibase-status`
3. **Apply changes** with `make liquibase-update`
4. **Start developing** new features with proper database changes
