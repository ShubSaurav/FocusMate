#!/bin/bash

# FocusMate - Run Script
# This script starts the FocusMate web application

echo "🎯 Starting FocusMate..."
echo "================================"

# Navigate to project directory
cd "$(dirname "$0")"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Clean and build the project (skip tests for faster startup)
echo "📦 Building project..."
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check for errors."
    exit 1
fi

echo "✅ Build successful!"
echo ""
echo "🚀 Starting Spring Boot application..."
echo "📍 Server will be available at: http://localhost:8081"
echo ""
echo "Press Ctrl+C to stop the server"
echo "================================"
echo ""

# Run the Spring Boot application
mvn spring-boot:run -q
