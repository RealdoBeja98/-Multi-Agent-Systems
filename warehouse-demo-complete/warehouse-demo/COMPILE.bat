@echo off
REM ========================================
REM BeaverWoodHome Warehouse MAS
REM Compilation Script for Windows
REM ========================================

echo.
echo ========================================
echo Compiling BeaverWoodHome Warehouse MAS
echo ========================================
echo.

REM Check if jade.jar exists
if not exist jade.jar (
    echo ERROR: jade.jar not found!
    echo Please download JADE 4.6.0 and place jade.jar in this folder
    echo Download from: https://jade.tilab.com/
    echo.
    pause
    exit /b 1
)

echo Found jade.jar
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java JDK 8 or higher
    echo Download from: https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

echo Java detected:
java -version
echo.

REM Compile all Java files
echo Compiling agents...
javac -cp jade.jar CoordinatorAgent.java TransportAgent.java ProductionStationAgent.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check the error messages above.
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS! Compilation complete.
echo ========================================
echo.
echo Generated files:
dir /b *.class
echo.
echo Next step: Run RUN.bat to start the system
echo.
pause
