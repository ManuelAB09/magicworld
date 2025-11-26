#!/usr/bin/env bash
set -euo pipefail

export DOCKER_BUILDKIT=1

# Determine script directory and important paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/.."
PROJECT_ROOT="$DOCKER_DIR/.."
COMPOSE_FILE="$DOCKER_DIR/docker-compose.yml"

# Locate .env preferably at project root
if [ -f "$PROJECT_ROOT/.env" ]; then
  ENV_FILE="$PROJECT_ROOT/.env"
elif [ -f "$DOCKER_DIR/.env" ]; then
  ENV_FILE="$DOCKER_DIR/.env"
elif [ -f .env ]; then
  ENV_FILE="$(pwd)/.env"
else
  echo ".env does not exist — copy .env.example to .env and check values. Aborting."
  exit 1
fi

# Verify docker-compose exists
if [ ! -f "$COMPOSE_FILE" ]; then
  echo "docker-compose not found at $COMPOSE_FILE. Aborting."
  exit 1
fi

# Change to project root so relative build contexts are consistent
echo "Changing to project root: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

echo "Building images (compose: $COMPOSE_FILE, env: $ENV_FILE)..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build --parallel

echo "Starting containers in background..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --remove-orphans

echo "Showing service status..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
