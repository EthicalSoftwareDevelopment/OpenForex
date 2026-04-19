<#
.SYNOPSIS
A PowerShell script that simultaneously boots the Java Core Backend and the Deno Edge Frontend.

.DESCRIPTION
This script launches two separate PowerShell windows:
1. The Open Liberty Java Core API (via install-and-run-backend.ps1)
2. The Deno Edge React/WebSocket Proxy
#>

$ErrorActionPreference = 'Stop'

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "       OpenForex Full Stack Runner                        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Use internal relative paths
$InvocationPath = $PSScriptRoot
$FrontendPath = [System.IO.Path]::GetFullPath((Join-Path -Path $InvocationPath -ChildPath "..\frontend"))

# Default ports mapping
$BackendPort = 9080
$FrontendPort = 8000

# 0. Validate Deno 2 Requirement First
try {
    $denoVersion = (deno --version 2>&1) -join " "
    if ($denoVersion -match "deno 2\.") {
        Write-Host "[OK] Deno 2.x Runtime Confirmed" -ForegroundColor Green
    } else {
        Write-Warning "[WARN] Deno 2.x is recommended for OpenForex, but found: $denoVersion"
    }
} catch {
    Write-Error "[ERROR] Deno is not installed or not in PATH. Please install Deno 2 (https://deno.land) and restart your console."
    exit 1
}

# 1. Start the Backend
Write-Host "`nStarting Java Backend (Open Liberty) in a new window..." -ForegroundColor Yellow
$BackendCmd = "try { `$host.ui.RawUI.WindowTitle='Open Liberty: JVM Core'; & `"$InvocationPath\install-and-run-backend.ps1`" -LibertyPort $BackendPort } catch { } ; Read-Host `'Press Enter to exit`'"
Start-Process powershell.exe -ArgumentList "-NoExit -ExecutionPolicy Bypass -Command `"$BackendCmd`"" -WindowStyle Normal

# 2. Wait a moment to stagger logging
Start-Sleep -Seconds 3

# 3. Start the Frontend
Write-Host "Starting Deno Frontend Proxy in a new window..." -ForegroundColor Yellow
$FrontendCmd = "`$host.ui.RawUI.WindowTitle='Deno 2: React Edge Proxy'; cd `"$FrontendPath`"; `$env:JAVA_BACKEND_URL='http://localhost:$BackendPort'; `$env:PORT='$FrontendPort'; deno task start ; Read-Host `'Press Enter to exit`'"
Start-Process powershell.exe -ArgumentList "-NoExit -Command `"$FrontendCmd`"" -WindowStyle Normal

Write-Host "`nAll services have been dispatched to new windows!" -ForegroundColor Green
Write-Host "----------------------------------------------------------" -ForegroundColor Cyan
Write-Host " Backend (Java JVM): Booting on http://localhost:$BackendPort" -ForegroundColor White
Write-Host " Frontend (Deno 2) : Booting on http://localhost:$FrontendPort" -ForegroundColor White
Write-Host "----------------------------------------------------------" -ForegroundColor Cyan
Write-Host "You can visit the frontend at: http://localhost:$FrontendPort" -ForegroundColor DarkCyan
Write-Host "You can view the Java OpenAPI spec at: http://localhost:$BackendPort/openapi/ui/" -ForegroundColor DarkCyan
