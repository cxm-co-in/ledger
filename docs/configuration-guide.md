# Configuration Guide

## 🎯 **Why We Use YAML Instead of Properties**

### **Problems with Multiple Properties Files**
- ❌ **Configuration conflicts** - Properties can override each other
- ❌ **Maintenance overhead** - Managing settings in multiple places  
- ❌ **Confusion** - Developers don't know which file to edit
- ❌ **Spring Boot precedence** - Properties files have complex loading order

### **Benefits of Single YAML File**
- ✅ **Single source of truth** - All configuration in one place
- ✅ **Profile-based configuration** - Clean separation of environments
- ✅ **Better readability** - Hierarchical structure is easier to understand
- ✅ **No conflicts** - Clear precedence rules
- ✅ **Easier maintenance** - One file to update

## 📁 **Configuration Structure**

```
src/main/resources/
└── application.yml          # Single configuration file
    ├── Common settings      # Shared across all profiles
    ├── Dev profile         # Development-specific settings
    └── Prod profile        # Production-specific settings
```

## 🔧 **Configuration Breakdown**

### **1. Common Settings (All Profiles)**
```yaml
spring:
  application:
    name: ledger
  
  # Liquibase Configuration
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
    contexts: dev
    default-schema: public
  
  # JPA/Hibernate Configuration
  jpa:
    hibernate:
      ddl-auto: none  # Let Liquibase handle schema
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/ledger
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  # DevTools Configuration
  devtools:
    restart:
      enabled: true
      additional-paths: src/main/java
      exclude: static/**,public/**,templates/**
```

### **2. Development Profile (`dev`)**
```yaml
---
spring:
  config:
    activate:
      on-profile: dev
  
  # Development-specific JPA settings
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  # Development logging
  logging:
    level:
      org.springframework.web: DEBUG
      com.cxm360.ai.ledger: DEBUG
      org.hibernate.orm.connections.pooling: WARN
      org.hibernate.orm.connections: WARN
  
  # Actuator endpoints for development
  management:
    endpoints:
      web:
        exposure:
          include: health,info,env,configprops
```

### **3. Production Profile (`prod`)**
```yaml
---
spring:
  config:
    activate:
      on-profile: prod
  
  # Production logging
  logging:
    level:
      com.cxm360.ai.ledger: INFO
      org.springframework.web: WARN
  
  # Production JPA settings
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
```

## 🚀 **How to Use Profiles**

### **Development Mode (Auto-reload + Debug)**
```bash
make dev          # Activates 'dev' profile automatically
# or manually:
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### **Production Mode (Minimal logging)**
```bash
make run          # Uses default profile (prod)
# or manually:
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### **Custom Profile**
```bash
./gradlew bootRun --args='--spring.profiles.active=staging'
```

## 🔄 **Profile Activation Priority**

Spring Boot loads configuration in this order (highest to lowest priority):

1. **Command line arguments** (`--spring.profiles.active=dev`)
2. **Environment variables** (`SPRING_PROFILES_ACTIVE=dev`)
3. **JVM system properties** (`-Dspring.profiles.active=dev`)
4. **application.yml** (profile-specific sections)
5. **application.yml** (common section)

## 🎨 **YAML vs Properties Comparison**

### **Properties File (Old Way)**
```properties
# application.properties
spring.application.name=ledger
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml

# application-dev.properties  
spring.jpa.show-sql=true
logging.level.com.cxm360.ai.ledger=DEBUG
```

### **YAML File (New Way)**
```yaml
# application.yml
spring:
  application:
    name: ledger
  
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
  
  # Dev profile
  config:
    activate:
      on-profile: dev
  
  jpa:
    show-sql: true
  
  logging:
    level:
      com.cxm360.ai.ledger: DEBUG
```

## 🚨 **Important Notes**

### **1. Profile Activation**
- **`dev` profile** is automatically activated with `make dev`
- **`prod` profile** is the default (no profile specified)
- **Profiles are additive** - common settings + profile-specific settings

### **2. Configuration Overrides**
- **Profile-specific settings override common settings**
- **Environment variables override YAML settings**
- **Command line arguments override everything**

### **3. Database Configuration**
- **Database settings are in common section** (shared across profiles)
- **Can be overridden by environment variables** (`.env` file)
- **Liquibase runs in all profiles** (enabled globally)

## 🎯 **Best Practices**

### **1. Configuration Organization**
- **Common settings** at the top
- **Profile-specific settings** in separate sections
- **Use consistent indentation** (2 spaces)
- **Group related settings** logically

### **2. Environment-Specific Settings**
- **Development**: Verbose logging, SQL output, debug info
- **Production**: Minimal logging, no SQL output, security-focused
- **Staging**: Similar to production but with some debugging

### **3. Security Considerations**
- **Never commit sensitive data** (passwords, API keys)
- **Use environment variables** for production secrets
- **Keep development settings** safe for local development

## 🔍 **Troubleshooting**

### **Profile Not Activating**
```bash
# Check active profile
./gradlew bootRun --args='--spring.profiles.active=dev' --debug

# Verify profile in logs
grep "The following profiles are active" logs/application.log
```

### **Configuration Not Loading**
```bash
# Check YAML syntax
yamllint src/main/resources/application.yml

# Verify file location
ls -la src/main/resources/application.yml
```

### **Profile Conflicts**
```bash
# Clear any cached profiles
./gradlew clean
# Restart application
```

## 🎉 **Benefits of This Approach**

1. **Single configuration file** - Easy to maintain
2. **Clear profile separation** - No confusion about settings
3. **Better readability** - Hierarchical structure
4. **No conflicts** - Clear precedence rules
5. **Environment-specific settings** - Tailored for each use case
6. **Easy to extend** - Add new profiles as needed

## 🚀 **Next Steps**

1. **Use `make dev`** for development (auto-reload + debug)
2. **Use `make run`** for production-like testing
3. **Customize settings** in `application.yml` as needed
4. **Add new profiles** (staging, testing) when required

**Your configuration is now clean, organized, and easy to maintain!** 🎯
