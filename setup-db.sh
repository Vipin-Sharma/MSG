#!/bin/bash

# MSG Database Setup Script for Linux/Mac
# Sets up SQL Server with Sakila sample database using Docker

set -e

echo "🚀 MSG Database Setup"
echo "===================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "   Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose is not available. Please install Docker Compose."
    echo "   Visit: https://docs.docker.com/compose/install/"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is ready"
echo ""

# Use docker compose if available, fallback to docker-compose
if docker compose version &> /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

echo "🐳 Starting SQL Server with Sakila database..."
echo "   This will:"
echo "   - Build custom SQL Server image with Sakila database"  
echo "   - Start container on port 1433"
echo "   - Set up 599 customers, 1000 films, 16k+ rentals"
echo ""

# Start the services
$COMPOSE_CMD up -d --build

echo ""
echo "✅ Database setup completed!"
echo ""
echo "📋 Connection Details:"
echo "   Server: localhost:1433"
echo "   Database: sakila"  
echo "   Username: sa"
echo "   Password: Password@1"
echo ""
echo "🎯 Ready to generate microservices! Try:"
echo "   mvn exec:java -Dexec.mainClass=\"com.jfeatures.msg.codegen.MicroServiceGenerator\" \\"
echo "     -Dexec.args=\"--name Customer --destination ./output --sql-file sample_parameterized_sql.sql\""
echo ""
echo "🛑 To stop: $COMPOSE_CMD down"
echo "🔄 To restart: $COMPOSE_CMD up -d"