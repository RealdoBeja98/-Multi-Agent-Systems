# ========================================
# BeaverWoodHome Warehouse MAS
# PowerShell Compilation Script
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Compiling BeaverWoodHome Warehouse MAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if jade.jar exists
if (-not (Test-Path "jade.jar")) {
    Write-Host "ERROR: jade.jar not found!" -ForegroundColor Red
    Write-Host "Please download JADE 4.6.0 and place jade.jar in this folder"
    Write-Host "Download from: https://jade.tilab.com/"
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✓ Found jade.jar" -ForegroundColor Green
Write-Host ""

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1
    Write-Host "✓ Java detected:" -ForegroundColor Green
    Write-Host $javaVersion[0]
    Write-Host ""
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java JDK 8 or higher"
    Write-Host "Download from: https://www.oracle.com/java/technologies/downloads/"
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

# Compile all Java files
Write-Host "Compiling agents..." -ForegroundColor Yellow
javac -cp jade.jar CoordinatorAgent.java TransportAgent.java ProductionStationAgent.java

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Compilation failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above."
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "SUCCESS! Compilation complete." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Generated files:"
Get-ChildItem *.class | ForEach-Object { Write-Host "  - $($_.Name)" }
Write-Host ""
Write-Host "Next step: Run " -NoNewline
Write-Host ".\RUN.ps1" -ForegroundColor Yellow -NoNewline
Write-Host " to start the system"
Write-Host ""
Read-Host "Press Enter to continue"
