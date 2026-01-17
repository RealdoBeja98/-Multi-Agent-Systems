@echo off
REM ========================================
REM BeaverWoodHome Warehouse MAS
REM Execution Script for Windows
REM ========================================

echo.
echo ========================================
echo BeaverWoodHome Warehouse MAS Prototype
echo ========================================
echo.
echo This demo shows autonomous robot coordination
echo using Multi-Agent Systems technology.
echo.
echo System Components:
echo - 1 CoordinatorAgent (manages task allocation)
echo - 4 TransportAgents (autonomous robots)
echo - 2 ProductionStationAgents (manufacturing stations)
echo.
echo Watch the console output to see:
echo 1. Agents starting up and registering
echo 2. Production stations requesting materials
echo 3. Robots bidding on tasks (Contract Net Protocol)
echo 4. Selected robot executing transport
echo 5. Task completion and statistics
echo.
echo The JADE GUI will also open - you can:
echo - See all active agents
echo - Use "Sniffer Agent" to visualize messages
echo - Use "Introspector Agent" to inspect behaviors
echo.
echo ========================================
echo.

REM Check if compiled
if not exist CoordinatorAgent.class (
    echo ERROR: Classes not compiled!
    echo Please run COMPILE.bat first
    echo.
    pause
    exit /b 1
)

echo Starting JADE platform with agents...
echo.
echo Press Ctrl+C to stop the system when done observing.
echo.
timeout /t 3

REM Run JADE with all agents
REM CRITICAL: NO SPACES after semicolons in agent list!
java -cp jade.jar;. jade.Boot -gui coordinator:CoordinatorAgent;robot1:TransportAgent(5,5);robot2:TransportAgent(15,5);robot3:TransportAgent(5,15);robot4:TransportAgent(15,15);sawing:ProductionStationAgent(SAWING);drilling:ProductionStationAgent(DRILLING)

echo.
echo System stopped.
pause


