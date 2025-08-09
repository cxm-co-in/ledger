
## Prerequisites
- Java 21
- Docker + Docker Compose

## Setup
1. Copy env file:
   ```bash
   cp .env.example .env
   ```
2. Start PostgreSQL:
   ```bash
   docker compose up -d
   ```
3. Build and run the app:
   ```bash
   ./gradlew bootRun
   ```

## Configuration
- App config in `src/main/resources/application.yml` uses env vars with defaults.
- Default DB: `general_ledger`, user: `general_ledger`, password: `general_ledger`.

## Useful Commands
```bash
./gradlew build        # Compile and run checks
./gradlew test         # Run tests
./gradlew bootRun      # Run app
docker compose up -d   # Start Postgres
```
