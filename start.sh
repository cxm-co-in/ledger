#!/bin/bash

# General Ledger Application Startup Script
# This script helps you quickly start the application with Docker

set -e

echo "🚀 Starting General Ledger Application..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if .env file exists, create if not
if [ ! -f .env ]; then
    echo "📝 Creating .env file with default values..."
    make env
fi

# Build and start services
echo "🔨 Building and starting services..."
make docker-build

echo ""
echo "✅ Services are starting up!"
echo ""
echo "📊 Service Status:"
docker compose ps
echo ""
echo "📋 Useful commands:"
echo "  View logs:        docker compose logs -f"
echo "  Stop services:    make docker-down"
echo "  Restart:          make docker-rebuild"
echo "  Health check:     curl http://localhost:8080/actuator/health"
echo "  Database health:  curl http://localhost:8080/actuator/health/db"
echo ""
echo "📝 Log Management:"
echo "  All logs:         make logs"
echo "  App logs only:    make logs-app"
echo "  DB logs only:     make logs-db"
echo "  Last 100 lines:   make logs-tail"
echo ""
echo "🌐 Application will be available at: http://localhost:8080"
echo "🗄️  Database will be available at: localhost:5432"
echo ""
echo "⏳ Waiting for services to be ready..."
echo "   (This may take a minute on first run)"

# Wait for services to be healthy
timeout=120
counter=0
while [ $counter -lt $timeout ]; do
    if docker compose ps | grep -q "healthy"; then
        echo "✅ All services are healthy!"
        break
    fi
    echo -n "."
    sleep 2
    counter=$((counter + 2))
done

if [ $counter -eq $timeout ]; then
    echo ""
    echo "⚠️  Timeout waiting for services. Check logs with: docker compose logs"
    exit 1
fi

echo ""
echo "🎉 General Ledger Application is ready!"
echo "   Visit: http://localhost:8080"
echo ""
echo "🔍 Test actuator endpoints:"
echo "   Main health:     curl http://localhost:8080/actuator/health"
echo "   Database health: curl http://localhost:8080/actuator/health/db"
echo "   Actuator index:  curl http://localhost:8080/actuator"
