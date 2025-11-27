#!/usr/bin/env bash
# Explicación:
# Script para parar y borrar los contenedores y volúmenes creados por docker-compose

set -euo pipefail

# Determinar directorio del script y rutas importantes
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/.."
PROJECT_ROOT="$DOCKER_DIR/.."
COMPOSE_FILE="$DOCKER_DIR/docker-compose.yml"

# Localizar .env preferentemente en la raíz del proyecto
if [ -f "$PROJECT_ROOT/.env" ]; then
  ENV_FILE="$PROJECT_ROOT/.env"
elif [ -f "$DOCKER_DIR/.env" ]; then
  ENV_FILE="$DOCKER_DIR/.env"
elif [ -f .env ]; then
  ENV_FILE="$(pwd)/.env"
else
  ENV_FILE=""
fi

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "No se encuentra docker-compose en $COMPOSE_FILE. Abortando."
  exit 1
fi

# Cambiar al root del proyecto para que las rutas relativas funcionen siempre
echo "Cambiando al directorio del proyecto: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

echo "Parando y eliminando contenedores y volúmenes..."
if [ -z "$ENV_FILE" ]; then
  echo "Parando contenedores sin archivo .env (se usarán variables de entorno actuales)..."
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans
else
  docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down -v --remove-orphans
fi
