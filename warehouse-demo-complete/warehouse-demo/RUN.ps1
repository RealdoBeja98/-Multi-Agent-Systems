# ========================================
# BeaverWoodHome Warehouse MAS
# PowerShell Execution Script
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BeaverWoodHome Warehouse MAS Prototype" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This demo shows autonomous robot coordination" -ForegroundColor White
Write-Host "using Multi-Agent Systems technology." -ForegroundColor White
Write-Host ""
Write-Host "System Components:" -ForegroundColor Yellow
Write-Host "  • 1 CoordinatorAgent (manages task allocation)"
Write-Host "  • 4 TransportAgents (autonomous robots)"
Write-Host "  • 2 ProductionStationAgents (manufacturing stations)"
Write-Host ""
Write-Host "Watch the console output to see:" -ForegroundColor Yellow
Write-Host "  1. Agents starting up and registering"
Write-Host "  2. Production stations requesting materials"
Write-Host "  3. Robots bidding on tasks (Contract Net Protocol)"
Write-Host "  4. Selected robot executing transport"
Write-Host "  5. Task completion and statistics"
Write-Host ""
Write-Host "The JADE GUI will also open where you can:" -ForegroundColor Yellow
Write-Host "  • See all active agents"
Write-Host "  • Use 'Sniffer Agent' to visualize messages"
Write-Host "  • Use 'Introspector Agent' to inspect behaviors"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if compiled
if (-not (Test-Path "CoordinatorAgent.class")) {
    Write-Host "ERROR: Classes not compiled!" -ForegroundColor Red
    Write-Host "Please run " -NoNewline
    Write-Host ".\COMPILE.ps1" -ForegroundColor Yellow -NoNewline
    Write-Host " first"
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Starting JADE platform with agents..." -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C to stop the system when done observing." -ForegroundColor Yellow
Write-Host ""
Start-Sleep -Seconds 2

# Run JADE with all agents
# Note: On Windows, use semicolon (;) as classpath separator
java -cp "jade.jar;." jade.Boot -gui `
    coordinator:CoordinatorAgent `
    robot1:TransportAgent(5,5) `
    robot2:TransportAgent(15,5) `
    robot3:TransportAgent(5,15) `
    robot4:TransportAgent(15,15) `
    sawing:ProductionStationAgent(SAWING) `
    drilling:ProductionStationAgent(DRILLING)

Write-Host ""
Write-Host "System stopped." -ForegroundColor Yellow
Read-Host "Press Enter to exit"
