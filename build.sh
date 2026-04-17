#!/bin/bash
# Build script for Remote Control Application

echo "=== Building Remote Control Application ==="

# Step 1: Build Server JAR
echo ""
echo "Step 1: Building Server JAR..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Server build failed!"
    exit 1
fi

echo "✅ Server JAR built: target/remote-server.jar"

# Step 2: Build Client JAR
echo ""
echo "Step 2: Building Client JAR..."
mvn clean package -Pclient -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Client build failed!"
    exit 1
fi

echo "✅ Client JAR built: target/remote-client.jar"

# Step 3: Create output directory
mkdir -p build

# Step 4: Convert to EXE (if Launch4j is installed)
if command -v launch4jc &> /dev/null; then
    echo ""
    echo "Step 3: Converting JARs to EXE..."
    launch4jc launch4j-server.xml
    launch4jc launch4j-client.xml
    echo "✅ EXE files created in build/"
else
    echo ""
    echo "⚠️  Launch4j not found. Skipping EXE conversion."
    echo "    Install Launch4j from: https://launch4j.sourceforge.net/"
    echo "    Or run JARs directly with: java -jar target/remote-server.jar"
fi

echo ""
echo "=== Build Complete! ==="
echo ""
echo "📦 Artifacts:"
echo "  - target/remote-server.jar"
echo "  - target/remote-client.jar"
if [ -f "build/RemoteServer.exe" ]; then
    echo "  - build/RemoteServer.exe"
    echo "  - build/RemoteClient.exe"
fi

echo ""
echo "🚀 To run:"
echo "  Server: java -jar target/remote-server.jar"
echo "  Client: java -jar target/remote-client.jar"
