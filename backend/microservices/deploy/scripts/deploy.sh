#!/usr/bin/env bash
# Pulls the requested image tag from GHCR, (re)starts the stack and waits until it is healthy.
#   ./scripts/deploy.sh                      # IMAGE_TAG from .env.prod (default latest)
#   IMAGE_TAG=sha-1a2b3c4 ./scripts/deploy.sh   # deploy / roll back to a specific build
#   PROFILES=observability ./scripts/deploy.sh  # also run Prometheus, Loki, Grafana
set -euo pipefail
cd "$(dirname "$0")/.."
[ -f .env.prod ] || { echo ".env.prod missing (copy .env.prod.example)"; exit 1; }
set -a; source .env.prod; set +a
export IMAGE_TAG=${IMAGE_TAG:-latest}
PROFILE_ARGS=""; for p in ${PROFILES:-}; do PROFILE_ARGS="$PROFILE_ARGS --profile $p"; done
COMPOSE="docker compose -f docker-compose.prod.yml --env-file .env.prod $PROFILE_ARGS"

if [ -n "${GHCR_TOKEN:-}" ]; then
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "${GHCR_USER:-$GHCR_OWNER}" --password-stdin >/dev/null
fi

echo "==> Deploying image tag: $IMAGE_TAG"
$COMPOSE pull --quiet
$COMPOSE up -d --remove-orphans --wait --wait-timeout 600

echo "==> Status"
$COMPOSE ps --format 'table {{.Service}}\t{{.Status}}'

echo "==> Public health check"
curl -sf "https://$API_DOMAIN/actuator/health" | jq -r .status || echo "warning: https://$API_DOMAIN not reachable yet"

docker image prune -f --filter "until=168h" >/dev/null
echo "$(date -Is) deployed $IMAGE_TAG" >> "$HOME/deployments.log"
