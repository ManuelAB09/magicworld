
$ErrorActionPreference = "Stop"

$env:DOCKER_BUILDKIT = "1"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DockerDir = Split-Path -Parent $ScriptDir
$ProjectRoot = Split-Path -Parent $DockerDir
$ComposeFile = Join-Path $DockerDir "docker-compose.yml"


$EnvFile = ""
if (Test-Path (Join-Path $ProjectRoot ".env")) {
    $EnvFile = Join-Path $ProjectRoot ".env"
} elseif (Test-Path (Join-Path $DockerDir ".env")) {
    $EnvFile = Join-Path $DockerDir ".env"
} elseif (Test-Path ".env") {
    $EnvFile = Join-Path (Get-Location) ".env"
} else {
    Write-Host ".env does not exist - copy .env.example to .env and check values. Aborting." -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $ComposeFile)) {
    Write-Host "docker-compose not found at $ComposeFile. Aborting." -ForegroundColor Red
    exit 1
}

Write-Host "Changing to project root: $ProjectRoot"
Set-Location $ProjectRoot

Write-Host "Building images (compose: $ComposeFile, env: $EnvFile)..."
docker compose -f $ComposeFile --env-file $EnvFile build --parallel

Write-Host "Starting containers in background..."
docker compose -f $ComposeFile --env-file $EnvFile up -d --remove-orphans

Write-Host "Showing service status..."
docker compose -f $ComposeFile --env-file $EnvFile ps

