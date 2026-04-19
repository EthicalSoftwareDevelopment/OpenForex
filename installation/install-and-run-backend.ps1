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

# Only stop Java processes related to Open Liberty (wlp or openforex-core in command line)
Write-Host "`n[1/4] Stopping any existing Open Liberty Java processes (cleaning up old instances)..." -ForegroundColor Yellow
$libertyProcs = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    try {
        $_.Path -and ((Get-Process -Id $_.Id -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Path) -match 'wlp|openforex-core')
    } catch { $false }
    # Fallback: check command line if available
    $cmd = (Get-CimInstance Win32_Process -Filter "ProcessId=$($_.Id)").CommandLine
    $cmd -and ($cmd -match 'wlp|openforex-core')
}
if ($libertyProcs) {
    $libertyProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "Stopped $($libertyProcs.Count) Open Liberty Java process(es)." -ForegroundColor Yellow
} else {
    Write-Host "No Open Liberty Java processes found to stop." -ForegroundColor Yellow
}
Start-Sleep -Seconds 2

# ===================== USER CONFIGURABLE VARIABLES =====================
# Set this to the absolute or relative path to your backend/core directory
$BackendCoreDir = "..\backend\core"  # Change this if your directory structure is different
# =======================================================================

# ===================== SCRIPT PARAMETERS =====================
param(
    [string]$MavenProfile = "",
    [int]$LibertyPort = 9081,
    [switch]$Help
)
# ============================================================

if ($Help -or $PSBoundParameters.ContainsKey('?')) {
    Write-Host "Usage: .\install-and-run-backend.ps1 [-MavenProfile <profile>] [-LibertyPort <port>] [-Help]" -ForegroundColor Cyan
    Write-Host "\nOptions:" -ForegroundColor Cyan
    Write-Host "  -MavenProfile <profile>   Specify a Maven profile to use (optional)" -ForegroundColor Cyan
    Write-Host "  -LibertyPort <port>       Override the default Liberty HTTP port (optional, default: 9081)" -ForegroundColor Cyan
    Write-Host "  -Help                    Show this help message and exit" -ForegroundColor Cyan
    exit 0
}

# Check for required tools: mvn and java
function Test-CommandExists {
    param([string]$Command)
    $null -ne (Get-Command $Command -ErrorAction SilentlyContinue)
}

if (-not (Test-CommandExists 'mvn')) {
    Write-Error "Maven (mvn) is not installed or not in PATH. Please install Maven and try again."
    exit 1
}
if (-not (Test-CommandExists 'java')) {
    Write-Error "Java is not installed or not in PATH. Please install Java and try again."
    exit 1
}

# Determine the absolute path to the backend/core folder relative to where this script is placed
$CoreBackendPath = [System.IO.Path]::GetFullPath((Join-Path -Path $PSScriptRoot -ChildPath $BackendCoreDir))

# Helper function to robustly delete a directory with retries (handles file locks on Windows)
function Remove-DirectoryWithRetry {
    param(
        [string]$Path,
        [int]$MaxRetries = 5,
        [int]$DelaySeconds = 2
    )
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            if (Test-Path $Path) {
                Remove-Item -Path $Path -Recurse -Force -ErrorAction Stop
                if (-not (Test-Path $Path)) {
                    return $true
                }
            } else {
                return $true
            }
        } catch {
            if ($i -eq $MaxRetries) {
                Write-Error "Failed to delete directory after $MaxRetries attempts: $Path. Error: $($_.Exception.Message)"
                return $false
            } else {
                Write-Host "Directory still locked, retrying in $DelaySeconds seconds... (Attempt $i/$MaxRetries)" -ForegroundColor DarkYellow
                Start-Sleep -Seconds $DelaySeconds
            }
        }
    }
    return $false
}

# Validate that $CoreBackendPath is not null or empty before using it
if (-Not $CoreBackendPath) {
    Write-Error "The CoreBackendPath variable is not set."
    exit 1
}

if (-Not (Test-Path -Path $CoreBackendPath)) {
    Write-Error "Cannot find the backend folder at: $CoreBackendPath"
    exit 1
}

Write-Host "`n[2/4] Navigating to target directory: $CoreBackendPath" -ForegroundColor Yellow
Set-Location -Path $CoreBackendPath

Write-Host "`n[3/4] Ensuring clean build space..." -ForegroundColor Yellow
if (Test-Path "$CoreBackendPath\target") {
    Write-Host "Attempting to delete target directory with retry logic..." -ForegroundColor Yellow
    $deleted = Remove-DirectoryWithRetry -Path "$CoreBackendPath\target" -MaxRetries 5 -DelaySeconds 2
    if (-not $deleted) {
        Write-Error "Could not clean the build space. Please ensure no Java processes are running and try again."
        exit 1
    }
}

Write-Host "`n[4/4] Starting Open Liberty (Resolving POM, compiling, and running)..." -ForegroundColor Yellow
Write-Host "Important Endpoints Once Running:" -ForegroundColor Green
Write-Host "   - Core API:       http://localhost:$LibertyPort/core/api" -ForegroundColor White
Write-Host "   - OpenAPI JSON:   http://localhost:$LibertyPort/openapi/" -ForegroundColor White
Write-Host "   - Swagger UI:     http://localhost:$LibertyPort/openapi/ui/" -ForegroundColor White
Write-Host "   - Health Checks:  http://localhost:$LibertyPort/health" -ForegroundColor White
Write-Host "   - Unauth Metrics: http://localhost:$LibertyPort/metrics" -ForegroundColor White
Write-Host "`nServer logs will stream below. Press 'Ctrl + C' to stop." -ForegroundColor Gray
Write-Host "----------------------------------------------------------`n" -ForegroundColor Cyan

# We use cmd.exe /c here because native PowerShell intercepts Maven's interactive console output
# in odd ways sometimes, giving a much better stream experience.
$logFile = Join-Path $PSScriptRoot "backend-run.log"
Write-Host "\nMaven output will be logged to: $logFile" -ForegroundColor Gray
cmd.exe /c "mvn clean package liberty:create liberty:install-feature -P$MavenProfile liberty:run -Dserver.port=$LibertyPort | tee $logFile"
