SHELL := /bin/bash

# Homebrew binary (if present)
BREW := $(shell command -v brew 2>/dev/null)

# Java 21 (Homebrew OpenJDK 21) home path
JHOME := $(shell if [ -x "$(BREW)" ]; then "$(BREW)" --prefix openjdk@21 2>/dev/null; fi)/libexec/openjdk.jdk/Contents/Home

# Gradle wrapper invocation pinned to Java 21 for this repo only
GRADLE := JAVA_HOME="$(JHOME)" PATH="$(JHOME)/bin:$(PATH)" ./gradlew -Dorg.gradle.java.home="$(JHOME)"

.PHONY: help java-path check-java install-java21 build test run clean docker-up docker-down env

help:
	@echo "Targets:"
	@echo "  install-java21  Install Homebrew OpenJDK 21 (required once)"
	@echo "  java-path       Print detected Java 21 home path"
	@echo "  build           Clean build using Java 21 only for this repo"
	@echo "  test            Run tests using Java 21 only for this repo"
	@echo "  run             Run the app (bootRun) using Java 21 only for this repo"
	@echo "  clean           Gradle clean"
	@echo "  docker-up       Start PostgreSQL via docker compose"
	@echo "  docker-down     Stop PostgreSQL"
	@echo "  env             Create .env from .env.example if missing"

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
	$(GRADLE) bootRun

.PHONY: dev
dev: run

clean: check-java
	$(GRADLE) clean

docker-up:
	docker compose up -d

docker-down:
	docker compose down

env:
	@[ -f .env ] || cp .env.example .env
	@echo ".env ready"


