#!/bin/bash
set -e

echo "🧪 Testing Docker setup instructions..."

# Test 1: Check if Docker is available
echo "1️⃣  Testing Docker availability..."
if command -v docker &> /dev/null; then
    echo "✅ Docker command is available"
else
    echo "❌ Docker command not found"
    exit 1
fi

# Test 2: Check if Docker Compose file is valid
echo "2️⃣  Testing Docker Compose configuration..."
if [ -f "docker-compose.yml" ]; then
    if docker-compose config &> /dev/null; then
        echo "✅ Docker Compose configuration is valid"
    else
        echo "❌ Docker Compose configuration is invalid"
        exit 1
    fi
else
    echo "❌ docker-compose.yml not found"
    exit 1
fi

# Test 3: Check if setup script is executable
echo "3️⃣  Testing setup script..."
if [ -x "setup-database.sh" ]; then
    echo "✅ setup-database.sh is executable"
else
    echo "❌ setup-database.sh is not executable"
    exit 1
fi

# Test 4: Validate setup script syntax
echo "4️⃣  Testing setup script syntax..."
if bash -n setup-database.sh; then
    echo "✅ setup-database.sh has valid syntax"
else
    echo "❌ setup-database.sh has syntax errors"
    exit 1
fi

# Test 5: Check application.properties matches Docker settings
echo "5️⃣  Testing configuration consistency..."
if grep -q "Password@1" src/main/resources/application.properties; then
    echo "✅ Application password matches Docker configuration"
else
    echo "❌ Password mismatch between application.properties and Docker setup"
    exit 1
fi

if grep -q "localhost:1433" src/main/resources/application.properties; then
    echo "✅ Database URL matches Docker configuration"
else
    echo "❌ Database URL mismatch"
    exit 1
fi

echo ""
echo "🎉 All Docker setup tests passed!"
echo ""
echo "📋 Summary:"
echo "   ✅ Docker command available"
echo "   ✅ Docker Compose configuration valid"  
echo "   ✅ Setup script ready and executable"
echo "   ✅ Script syntax validated"
echo "   ✅ Configuration consistency verified"
echo ""
echo "🚀 Ready to run: ./setup-database.sh"