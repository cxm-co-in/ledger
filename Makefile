SHELL := /bin/bash

# Homebrew binary (if present)
BREW := $(shell command -v brew 2>/dev/null)

# Java 21 (Homebrew OpenJDK 21) home path
JHOME := $(shell if [ -x "$(BREW)" ]; then "$(BREW)" --prefix openjdk@21 2>/dev/null; fi)/libexec/openjdk.jdk/Contents/Home

# Gradle wrapper invocation pinned to Java 21 for this repo only
GRADLE := JAVA_HOME="$(JHOME)" PATH="$(JHOME)/bin:$(PATH)" ./gradlew -Dorg.gradle.java.home="$(JHOME)"

.PHONY: help java-path check-java install-java21 build test run clean docker-up docker-down docker-build docker-rebuild env liquibase-status liquibase-update liquibase-rollback

help:
	@echo "Targets:"
	@echo "  install-java21  Install Homebrew OpenJDK 21 (required once)"
	@echo "  java-path       Print detected Java 21 home path"
	@echo "  build           Clean build using Java 21 only for this repo"
	@echo "  test            Run tests using Java 21 only for this repo"
	@echo "  run             Run the app with production profile (bootRun)"
	@echo "  run-env         Run the app with profile from .env file"
	@echo "  dev             Run the app with development profile and auto-reload"
	@echo "  watch           Run the app with development profile and auto-reload (continuous)"
	@echo "  run-profile     Run the app with a custom profile (usage: make run-profile PROFILE=dev)"
	@echo "  clean           Gradle clean"
	@echo "  docker-up       Start PostgreSQL and Ledger app via docker compose"
	@echo "  docker-down     Stop all services"
	@echo "  docker-build    Build and start all services"
	@echo "  docker-rebuild  Rebuild and restart all services"
	@echo "  env             Create .env from .env.example if missing"
	@echo ""
	@echo "Database Management (Liquibase):"
	@echo "  liquibase-status   Check Liquibase changelog status"
	@echo "  liquibase-update   Apply pending Liquibase changes"
	@echo "  liquibase-rollback Rollback last Liquibase change"
	@echo ""
	@echo "Configuration:"
	@echo "  Uses profile-specific application files:"
	@echo "    - application.yml (base config)"
	@echo "    - application-dev.yml (development profile)"
	@echo "    - application-prod.yml (production profile)"
	@echo "    - application-test.yml (test profile)"
	@echo "  Dev profile: auto-reload, SQL logging, debug logging"
	@echo "  Prod profile: minimal logging, no SQL output"

java-path:
	@echo "JHOME=$(JHOME)"
	@if [ ! -d "$(JHOME)" ]; then \
		echo "Java 21 not found at above path. Run: make install-java21"; \
		exit 1; \
	fi

check-java: java-path

install-java21:
	@if [ -z "$(BREW)" ]; then \
		echo "Homebrew not found. Install from https://brew.sh and re-run."; \
		exit 1; \
	fi
	@"$(BREW)" update || true
	@"$(BREW)" install openjdk@21 || true
	@echo "Installed/verified openjdk@21."

build: check-java
	$(GRADLE) clean build

test: check-java
	$(GRADLE) test

run: check-java env docker-up
	@echo "Starting application with production profile..."
	$(GRADLE) bootRun --args="--spring.profiles.active=prod"

run-env: check-java env docker-up
	@echo "Starting application with profile from .env file..."
	$(GRADLE) bootRun

.PHONY: dev watch run-profile
dev: check-java env docker-up
	@echo "Starting application with development profile and auto-reload..."
	$(GRADLE) bootRun --continuous --args="--spring.profiles.active=dev"

watch: check-java env docker-up
	@echo "Starting application with development profile and auto-reload (continuous mode)..."
	$(GRADLE) bootRun --continuous --args="--spring.profiles.active=dev"

run-profile: check-java env docker-up
	@if [ -z "$(PROFILE)" ]; then \
		echo "Error: PROFILE not specified. Usage: make run-profile PROFILE=dev"; \
		exit 1; \
	fi
	@echo "Starting application with $(PROFILE) profile..."
	$(GRADLE) bootRun --args="--spring.profiles.active=$(PROFILE)"

clean: check-java
	$(GRADLE) clean

docker-up:
	docker compose up -d

docker-down:
	docker compose down

docker-build: env
	docker compose up -d --build

docker-rebuild: env
	docker compose down
	docker compose up -d --build

env:
	@if [ ! -f .env ]; then \
		echo "Creating .env file with default values..."; \
		echo "DB_PORT=5432" > .env; \
		echo "POSTGRES_DB=general_ledger" >> .env; \
		echo "POSTGRES_USER=general_ledger" >> .env; \
		echo "POSTGRES_PASSWORD=general_ledger" >> .env; \
		echo "SPRING_PROFILES_ACTIVE=dev" >> .env; \
		echo "APP_PORT=8080" >> .env; \
		echo "JAVA_OPTS=-Xmx512m -Xms256m" >> .env; \
	fi
	@echo ".env ready"

# Liquibase Commands
liquibase-status: check-java env docker-up
	@echo "Liquibase runs automatically with Spring Boot"
	@echo "To check status, run the application and check logs"
	@echo "Or connect to database and check DATABASECHANGELOG table"

liquibase-update: check-java env docker-up
	@echo "Liquibase updates automatically when application starts"
	@echo "Run 'make run' or 'make dev' to start the application"

liquibase-rollback: check-java env docker-up
	@echo "Manual rollback not available with Spring Boot auto-execution"
	@echo "To rollback, manually update DATABASECHANGELOG table in database"


