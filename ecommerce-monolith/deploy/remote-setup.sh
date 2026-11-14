#!/usr/bin/env bash
# Runs ON the apps-server (uploaded and started by deploy.sh). Idempotent:
# first-run secrets, database role/database, start + health-check shop-api, reload Caddy.
# Every 'docker compose exec' reads from /dev/null so it can never consume this script's stdin.
set -euo pipefail
cd /opt/apps
test -f .env || { echo "Missing /opt/apps/.env"; exit 1; }

add_secret() {  # add_secret NAME VALUE  (only if NAME is not in .env yet)
  grep -q "^$1=" .env || { echo "$1=$2" >> .env; echo "   generated $1"; GENERATED=1; }
}
GENERATED=0
add_secret SHOP_DB_PASSWORD "$(openssl rand -hex 16)"
add_secret SHOP_JWT_SECRET "$(openssl rand -hex 32)"
add_secret SHOP_ADMIN_EMAIL "admin@shop.onekampus.com"
add_secret SHOP_ADMIN_PASSWORD "$(openssl rand -base64 18 | tr -d '/+=' | cut -c1-16)A1"
set -a; source .env; set +a

docker compose up -d postgres >/dev/null
for i in $(seq 1 30); do docker compose exec -T postgres pg_isready -U dabba -d dabba </dev/null >/dev/null 2>&1 && break; sleep 2; done
PSQL="docker compose exec -T postgres psql -U dabba -d dabba -tAq"
psql_q() { $PSQL -c "$1" </dev/null; }
if [ "$(psql_q "SELECT 1 FROM pg_roles WHERE rolname='shop'")" != "1" ]; then
  psql_q "CREATE ROLE shop LOGIN PASSWORD '${SHOP_DB_PASSWORD}'"
  echo "   created role shop"
fi
if [ "$(psql_q "SELECT 1 FROM pg_database WHERE datname='shopdb'")" != "1" ]; then
  psql_q "CREATE DATABASE shopdb OWNER shop"
  echo "   created database shopdb"
fi

docker compose up -d --build shop-api
for i in $(seq 1 60); do
  [ "$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q shop-api)")" = healthy ] && break; sleep 3
done
docker compose ps shop-api
docker compose exec -T caddy caddy reload </dev/null --config /etc/caddy/Caddyfile >/dev/null 2>&1 || true
docker image prune -f >/dev/null
if [ "$GENERATED" = 1 ]; then
  echo "   admin account: $SHOP_ADMIN_EMAIL (password: SHOP_ADMIN_PASSWORD in /opt/apps/.env)"
fi
