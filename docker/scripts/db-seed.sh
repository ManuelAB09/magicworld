#!/usr/bin/env sh
set -euo pipefail

echo "Seed script: esperando a que la base de datos acepte conexiones..."
for i in $(seq 1 60); do
  if mysql -h db -P 3306 -u${MYSQL_USER:-root} -p${MYSQL_PASSWORD:-$MYSQL_ROOT_PASSWORD} -e 'SELECT 1' >/dev/null 2>&1; then
    echo "DB listo"
    break
  fi
  echo "DB no lista, reintentando... ($i)"
  sleep 1
done

echo "Esperando a que el backend responda /actuator/health (aceptamos 200/401/403)..."
for i in $(seq 1 60); do
  status=$(curl -s -o /dev/null -w '%{http_code}' http://backend:8080/actuator/health || echo 0)
  if [ "$status" = "200" ] || [ "$status" = "401" ] || [ "$status" = "403" ]; then
    echo "Backend listo (status=$status)"
    break
  fi
  echo "Backend no listo (status=$status), reintentando... ($i)"
  sleep 1
done

if [ -f /seed/data.sql ]; then
  echo "Encontrado /seed/data.sql — ejecutando INSERTs (no creará tablas)"
  mysql -h db -P 3306 -u${MYSQL_USER:-root} -p${MYSQL_PASSWORD:-$MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} < /seed/data.sql || true
  echo "Seed aplicado (si hubo errores, se ignoraron)"
else
  echo "No se encontró /seed/data.sql — nada que aplicar"
fi

echo "Seed finalizado"

