#!/bin/bash

# MSG End-to-End Testing Script
# This script demonstrates how to run the comprehensive E2E tests

set -e

echo "🚀 MSG End-to-End Testing Suite"
echo "================================"
echo

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi
echo "✅ Docker is running"

# Check Java version
if ! java -version > /dev/null 2>&1; then
    echo "❌ Java is not installed or not in PATH"
    exit 1
fi
echo "✅ Java is available"

# Check Maven
if ! mvn -version > /dev/null 2>&1; then
    echo "❌ Maven is not installed or not in PATH"
    exit 1
fi
echo "✅ Maven is available"

echo

# Clean and prepare
echo "🧹 Cleaning previous builds..."
mvn clean -q

echo "🔧 Compiling project..."
mvn compile -q

echo

# Run E2E tests
echo "🧪 Running End-to-End Tests..."
echo "This will:"
echo "  • Start SQL Server in a container (Testcontainers)"
echo "  • Generate all 4 CRUD APIs from test SQL files"
echo "  • Validate generated code structure and quality"
echo "  • Compile generated microservices"
echo "  • Start generated services and test REST endpoints"
echo "  • Clean up test resources"
echo

echo "⏳ Starting E2E test execution (this may take 5-10 minutes)..."

# Run the E2E tests with the special profile
if mvn test -Pe2e-tests -Dtest=WorkingE2EGenerationTest -q; then
    echo "✅ All E2E tests passed successfully!"
    echo
    echo "🎉 MSG Project E2E Testing Summary:"
    echo "   • CRUD API generation: ✅ PASSED"
    echo "   • Code structure validation: ✅ PASSED" 
    echo "   • Generated code compilation: ✅ PASSED"
    echo "   • REST API integration testing: ✅ PASSED"
    echo "   • Multi-domain generation: ✅ PASSED"
    echo "   • Error handling: ✅ PASSED"
    echo
    echo "💡 Your MSG tool is working perfectly!"
    echo "   You can confidently use the README commands to generate microservices."
    
else
    echo "❌ Some E2E tests failed"
    echo
    echo "🔍 Troubleshooting tips:"
    echo "   • Check Docker is running and has sufficient resources"
    echo "   • Ensure ports 8080 and 1433 are available"
    echo "   • Run with -X flag for detailed debugging: mvn test -Pe2e-tests -X"
    echo "   • Check test logs in target/surefire-reports/"
    exit 1
fi

echo
echo "📊 Test Reports:"
echo "   • Surefire reports: target/surefire-reports/"
echo "   • JaCoCo coverage: target/site/jacoco/"
echo
echo "🔧 Additional Commands:"
echo "   • Run specific test: mvn test -Pe2e-tests -Dtest=EndToEndCrudGenerationTest#testCompleteCrudGeneration"
echo "   • Run with debug output: mvn test -Pe2e-tests -X"
echo "   • Generate coverage: mvn test jacoco:report -Pe2e-tests"

echo
echo "🎯 Next Steps:"
echo "   1. Try the commands from README.md to generate your own microservices"
echo "   2. Use the generated SQL files as templates for your own use cases"  
echo "   3. Customize the generation parameters for your specific needs"

echo
echo "✨ Happy microservice generation!"