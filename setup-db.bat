@echo off
setlocal

REM MSG Database Setup Script for Windows
REM Sets up SQL Server with Sakila sample database using Docker

echo.
echo 🚀 MSG Database Setup
echo ====================

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not installed. Please install Docker Desktop first.
    echo    Visit: https://docs.docker.com/desktop/windows/
    pause
    exit /b 1
)

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo ✅ Docker is ready
echo.

REM Check for Docker Compose
docker compose version >nul 2>&1
if errorlevel 1 (
    docker-compose --version >nul 2>&1
    if errorlevel 1 (
        echo ❌ Docker Compose is not available. Please update Docker Desktop.
        pause
        exit /b 1
    ) else (
        set COMPOSE_CMD=docker-compose
    )
) else (
    set COMPOSE_CMD=docker compose
)

echo 🐳 Starting SQL Server with Sakila database...
echo    This will:
echo    - Build custom SQL Server image with Sakila database
echo    - Start container on port 1433
echo    - Set up 599 customers, 1000 films, 16k+ rentals
echo.

REM Start the services
%COMPOSE_CMD% up -d --build

if errorlevel 1 (
    echo ❌ Failed to start database services
    pause
    exit /b 1
)

echo.
echo ✅ Database setup completed!
echo.
echo 📋 Connection Details:
echo    Server: localhost:1433
echo    Database: sakila
echo    Username: sa
echo    Password: Password@1
echo.
echo 🎯 Ready to generate microservices! Try:
echo    mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" ^
echo      -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"
echo.
echo 🛑 To stop: %COMPOSE_CMD% down
echo 🔄 To restart: %COMPOSE_CMD% up -d
echo.

pause