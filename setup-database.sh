#!/bin/bash
set -e

echo "🚀 Setting up SQL Server database for MSG..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "   Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is ready"

# Start SQL Server using Docker Compose
if [ -f "docker-compose.yml" ]; then
    echo "📦 Starting SQL Server with Docker Compose..."
    docker-compose up -d
else
    echo "📦 Starting SQL Server with Docker..."
    docker run -e "ACCEPT_EULA=Y" \
               -e "MSSQL_SA_PASSWORD=Password@1" \
               -e "MSSQL_PID=Developer" \
               -p 1433:1433 \
               -d \
               --name msgdb \
               mcr.microsoft.com/mssql/server:2022-latest
fi

echo "⏳ Waiting for SQL Server to start..."
sleep 30

# Wait for SQL Server to be ready
echo "🔍 Checking SQL Server connection..."
max_attempts=10
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker exec msgdb /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Password@1' -Q "SELECT 1" &> /dev/null; then
        echo "✅ SQL Server is ready!"
        break
    else
        echo "⏳ Attempt $attempt/$max_attempts: SQL Server not ready yet..."
        sleep 10
        ((attempt++))
    fi
done

if [ $attempt -gt $max_attempts ]; then
    echo "❌ SQL Server failed to start properly"
    exit 1
fi

# Create sakila database
echo "🗄️  Creating sakila database..."
docker exec msgdb /opt/mssql-tools/bin/sqlcmd \
   -S localhost -U SA -P 'Password@1' \
   -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'sakila') CREATE DATABASE sakila;"

echo "✅ Database setup complete!"
echo ""
echo "📋 Connection Details:"
echo "   Server: localhost:1433"
echo "   Database: sakila"
echo "   Username: sa"
echo "   Password: Password@1"
echo ""
echo "🎯 You can now generate microservices with commands like:"
echo "   mvn exec:java -Dexec.mainClass=\"com.jfeatures.msg.codegen.MicroServiceGenerator\" \\"
echo "     -Dexec.args=\"--name Customer --destination ./output --sql-file sample_parameterized_sql.sql\""
echo ""
echo "🛑 To stop SQL Server later: docker stop msgdb"
echo "🔄 To start SQL Server again: docker start msgdb"