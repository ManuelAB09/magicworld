

set -euo pipefail


SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/.."
PROJECT_ROOT="$DOCKER_DIR/.."
COMPOSE_FILE="$DOCKER_DIR/docker-compose.yml"


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


echo "Cambiando al directorio del proyecto: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

echo "Parando y eliminando contenedores y volúmenes..."
if [ -z "$ENV_FILE" ]; then
  echo "Parando contenedores sin archivo .env (se usarán variables de entorno actuales)..."
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans
else
  docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down -v --remove-orphans
fi
