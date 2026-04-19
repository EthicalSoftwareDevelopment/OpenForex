<#
.SYNOPSIS
A PowerShell script that installs Maven dependencies, compiles the OpenForex backend,
provisions the Open Liberty runtime, and starts the server.

.DESCRIPTION
This script automates the full workflow to run the backend core API.
It cleans up old target builds, kills hanging Java processes to prevent file locking/port issues,
and launches the server with dynamic feature installation.
#>

$ErrorActionPreference = 'Stop'

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "       OpenForex Backend Runner (Open Liberty)            " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

Write-Host "`n[1/4] Stopping any existing Java processes (cleaning up old instances)..." -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Determine the absolute path to the backend/core folder relative to where this script is placed
$CoreBackendPath = [System.IO.Path]::GetFullPath((Join-Path -Path $PSScriptRoot -ChildPath "..\backend\core"))
if (-Not (Test-Path -Path $CoreBackendPath)) {
    Write-Error "Cannot find the backend folder at: $CoreBackendPath"
    exit 1
}

Write-Host "`n[2/4] Navigating to target directory: $CoreBackendPath" -ForegroundColor Yellow
Set-Location -Path $CoreBackendPath

Write-Host "`n[3/4] Ensuring clean build space..." -ForegroundColor Yellow
if (Test-Path "$CoreBackendPath\target") {
    Remove-Item -Path "$CoreBackendPath\target" -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "`n[4/4] Starting Open Liberty (Resolving POM, compiling, and running)..." -ForegroundColor Yellow
Write-Host "Important Endpoints Once Running:" -ForegroundColor Green
Write-Host "   - Core API:       http://localhost:9081/core/api" -ForegroundColor White
Write-Host "   - OpenAPI JSON:   http://localhost:9081/openapi/" -ForegroundColor White
Write-Host "   - Swagger UI:     http://localhost:9081/openapi/ui/" -ForegroundColor White
Write-Host "   - Health Checks:  http://localhost:9081/health" -ForegroundColor White
Write-Host "   - Unauth Metrics: http://localhost:9081/metrics" -ForegroundColor White
Write-Host "`nServer logs will stream below. Press 'Ctrl + C' to stop." -ForegroundColor Gray
Write-Host "----------------------------------------------------------`n" -ForegroundColor Cyan

# We use cmd.exe /c here because native PowerShell intercepts Maven's interactive console output
# in odd ways sometimes, giving a much better stream experience.
cmd.exe /c "mvn clean package liberty:create liberty:install-feature liberty:run"

