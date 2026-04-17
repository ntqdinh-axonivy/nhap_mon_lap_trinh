@echo off
REM Build script for Remote Control Application (Windows)

echo === Building Remote Control Application ===

REM Step 1: Build Server JAR
echo.
echo Step 1: Building Server JAR...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo Server JAR built: target\remote-server.jar

REM Step 2: Build Client JAR
echo.
echo Step 2: Building Client JAR...
call mvn clean package -Pclient -DskipTests

if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo Client JAR built: target\remote-client.jar

REM Step 3: Create output directory
if not exist "build" mkdir build

REM Step 4: Convert to EXE
where launch4jc >/dev/null 2>/dev/null
if %errorlevel% == 0 (
    echo.
    echo Step 3: Converting JARs to EXE...
    launch4jc launch4j-server.xml
    launch4jc launch4j-client.xml
    echo EXE files created in build\
) else (
    echo.
    echo Launch4j not found. Skipping EXE conversion.
    echo Install from: https://launch4j.sourceforge.net/
)

echo.
echo === Build Complete! ===
echo.
echo Artifacts:
echo   - target\remote-server.jar
echo   - target\remote-client.jar
if exist "build\RemoteServer.exe" (
    echo   - build\RemoteServer.exe
    echo   - build\RemoteClient.exe
)

echo.
echo To run:
echo   Server: java -jar target\remote-server.jar
echo   Client: java -jar target\remote-client.jar

pause
