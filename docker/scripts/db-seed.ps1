$ErrorActionPreference = "Stop"

$MysqlUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }
$MysqlPassword = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { $env:MYSQL_ROOT_PASSWORD }
$MysqlDatabase = $env:MYSQL_DATABASE

Write-Host "Seed script: esperando a que la base de datos acepte conexiones..."
for ($i = 1; $i -le 60; $i++) {
    try {
        $result = mysql -h db -P 3306 -u$MysqlUser -p$MysqlPassword -e "SELECT 1" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "DB listo"
            break
        }
    } catch { }
    Write-Host "DB no lista, reintentando... ($i)"
    Start-Sleep -Seconds 1
}

Write-Host "Esperando a que el backend responda /actuator/health (aceptamos 200/401/403)..."
for ($i = 1; $i -le 60; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://backend:8080/actuator/health" -UseBasicParsing -ErrorAction SilentlyContinue
        $status = $response.StatusCode
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
        } else {
            $status = 0
        }
    }

    if ($status -eq 200 -or $status -eq 401 -or $status -eq 403) {
        Write-Host "Backend listo (status=$status)"
        break
    }
    Write-Host "Backend no listo (status=$status), reintentando... ($i)"
    Start-Sleep -Seconds 1
}

$SeedFile = "/seed/data.sql"
if (Test-Path $SeedFile) {
    Write-Host "Encontrado $SeedFile - ejecutando INSERTs (no creara tablas)"
    try {
        Get-Content $SeedFile | mysql -h db -P 3306 -u$MysqlUser -p$MysqlPassword $MysqlDatabase
        Write-Host "Seed aplicado"
    } catch {
        Write-Host "Seed aplicado (si hubo errores, se ignoraron)"
    }
} else {
    Write-Host "No se encontro $SeedFile - nada que aplicar"
}

Write-Host "Seed finalizado"

