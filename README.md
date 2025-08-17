# General Ledger Application

A Spring Boot-based general ledger system with PostgreSQL database and Liquibase for schema management.

## Prerequisites
- Java 21
- Docker + Docker Compose

## Quick Start with Docker

### Option 1: Run Everything with Docker Compose (Recommended)
```bash
# Create environment file with defaults
make env

# Build and start all services
make docker-build

# Or just start existing services
make docker-up

# Stop all services
make docker-down
```

### Option 2: Use the Start Script (Easiest)
```bash
# Make script executable (first time only)
chmod +x start.sh

# Start everything with one command
./start.sh
```

### Option 3: Manual Docker Compose
```bash
# Create environment file
cp .env.example .env  # or run: make env

# Build and start all services
docker compose up -d --build

# View logs
docker compose logs -f

# Stop services
docker compose down
```

## Development Setup

### 1. Environment Configuration
```bash
# Create .env file with default values
make env

# Customize .env file as needed:
# DB_PORT=5432
# POSTGRES_DB=general_ledger
# POSTGRES_USER=general_ledger
# POSTGRES_PASSWORD=general_ledger
# SPRING_PROFILES_ACTIVE=dev
# APP_PORT=8080
# JAVA_OPTS=-Xmx512m -Xms256m
```

### 2. Start Services
```bash
# Start PostgreSQL only (for local development)
make docker-up

# Or start everything with Docker
make docker-build
```

### 3. Run Application
```bash
# Run with development profile (auto-reload)
make dev

# Run with specific profile
make run-profile PROFILE=dev

# Run with production profile
make run
```

## Docker Services

The Docker Compose setup includes:

- **PostgreSQL 16**: Database service with health checks
- **Ledger App**: Spring Boot application with automatic database connection
- **Networking**: Isolated network for service communication
- **Volumes**: Persistent data storage for database and logs
- **Health Checks**: Ensures services are ready before starting dependent services

## Useful Commands

### Docker Management
```bash
make docker-up          # Start all services
make docker-down        # Stop all services
make docker-build       # Build and start services
make docker-rebuild     # Rebuild and restart services
```

### Application Development
```bash
make build             # Build the application
make test              # Run tests
make dev               # Run with development profile
make run               # Run with production profile
make clean             # Clean build artifacts
```

### Database Management
```bash
# Liquibase runs automatically with Spring Boot
# Check application logs for migration status
docker compose logs ledger
```

## Configuration

- **App Config**: `src/main/resources/application.yml` (base configuration)
- **Profile Configs**: 
  - `application-dev.yml` (development profile)
  - `application-prod.yml` (production profile)
  - `application-test.yml` (test profile)
- **Database**: Automatically configured via environment variables
- **Ports**: 
  - Application: 8080 (configurable via APP_PORT)
  - PostgreSQL: 5432 (configurable via DB_PORT)

## Troubleshooting

### Service Health Checks
```bash
# Check service status
docker compose ps

# View service logs
docker compose logs postgres
docker compose logs ledger

# Check health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/db
```

### Actuator Endpoints
The application provides several actuator endpoints for monitoring:

- **`/actuator/health`** - Overall application health status
- **`/actuator/health/db`** - Database connection health status  
- **`/actuator/info`** - Application information
- **`/actuator/env`** - Environment variables
- **`/actuator/configprops`** - Configuration properties

### Common Issues
1. **Port conflicts**: Change ports in `.env` file
2. **Memory issues**: Adjust `JAVA_OPTS` in `.env` file
3. **Database connection**: Ensure PostgreSQL is healthy before starting ledger app
4. **Build issues**: Run `make docker-rebuild` to clean rebuild
5. **Actuator endpoints not accessible**: Check if the application profile has actuator enabled

### Reset Everything
```bash
# Stop and remove all containers, networks, and volumes
docker compose down -v

# Rebuild from scratch
make docker-build
```
