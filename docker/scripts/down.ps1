$ErrorActionPreference = "Stop"

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
}

if (-not (Test-Path $ComposeFile)) {
    Write-Host "No se encuentra docker-compose en $ComposeFile. Abortando." -ForegroundColor Red
    exit 1
}

Write-Host "Cambiando al directorio del proyecto: $ProjectRoot"
Set-Location $ProjectRoot

Write-Host "Parando y eliminando contenedores y volumenes..."
if ([string]::IsNullOrEmpty($EnvFile)) {
    Write-Host "Parando contenedores sin archivo .env (se usaran variables de entorno actuales)..."
    docker compose -f $ComposeFile down -v --remove-orphans
} else {
    docker compose -f $ComposeFile --env-file $EnvFile down -v --remove-orphans
}
