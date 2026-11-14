#!/usr/bin/env bash
# =============================================================================
# Build the e-commerce monolith locally and (re)start it on the shared Lightsail
# apps-server (next to Dabba and the job portal), served at https://shop.onekampus.com.
#
#   ecommerce-monolith/deploy/deploy.sh            (from anywhere)
#
# What it does
#   1. builds the jar with Maven (tests skipped; CI runs them)
#   2. syncs the shared compose.yml + Caddyfile from the dabba-platform repo — the single
#      source of truth for that server (it contains the shop-api service and the shop site)
#   3. uploads the jar + runtime Dockerfile to /opt/apps/shop-api
#   4. first run only: generates SHOP_* secrets in /opt/apps/.env and creates the
#      "shop" role and "shopdb" database in the existing Postgres
#   5. starts shop-api, waits for health, reloads Caddy
#
# Env overrides: DEPLOY_HOST (ubuntu@16.4.118.190), SSH_KEY (~/.ssh/apps-server-lightsail.pem),
#                SHARED_DEPLOY_DIR (../../dabba-platform/deploy relative to this repo's root)
# =============================================================================
set -euo pipefail
HOST="${DEPLOY_HOST:-ubuntu@16.4.118.190}"
KEY="${SSH_KEY:-$HOME/.ssh/apps-server-lightsail.pem}"
REMOTE=/opt/apps
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SHARED="${SHARED_DEPLOY_DIR:-$(cd "$APP_DIR/../.." && pwd)/dabba-platform/deploy}"
SSH=(ssh -i "$KEY" "$HOST")

grep -q 'shop-api:' "$SHARED/compose.yml" || { echo "shop-api service missing in $SHARED/compose.yml"; exit 1; }
grep -q 'shop.onekampus.com' "$SHARED/Caddyfile" || { echo "shop site missing in $SHARED/Caddyfile"; exit 1; }

echo "==> Building jar"
(cd "$APP_DIR" && JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}" ./mvnw -B -q -DskipTests package)
JAR=$(ls "$APP_DIR"/target/ecommerce-monolith-*.jar | grep -v original | head -1)

echo "==> Shared server files (compose.yml, Caddyfile) from $SHARED"
rsync -az --inplace -e "ssh -i $KEY" "$SHARED/compose.yml" "$SHARED/Caddyfile" "$HOST:$REMOTE/"

echo "==> Uploading jar"
"${SSH[@]}" "mkdir -p $REMOTE/shop-api"
scp -q -i "$KEY" "$APP_DIR/deploy/Dockerfile" "$HOST:$REMOTE/shop-api/Dockerfile"
scp -q -i "$KEY" "$JAR" "$HOST:$REMOTE/shop-api/shop.jar"

echo "==> Secrets, database, start"
"${SSH[@]}" "bash -s" <<'REMOTE_SCRIPT'
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
for i in $(seq 1 30); do docker compose exec -T postgres pg_isready -U dabba -d dabba >/dev/null 2>&1 && break; sleep 2; done
PSQL="docker compose exec -T postgres psql -U dabba -d dabba -tAq"
if [ "$($PSQL -c "SELECT 1 FROM pg_roles WHERE rolname='shop'")" != "1" ]; then
  $PSQL -c "CREATE ROLE shop LOGIN PASSWORD '${SHOP_DB_PASSWORD}'"
  echo "   created role shop"
fi
if [ "$($PSQL -c "SELECT 1 FROM pg_database WHERE datname='shopdb'")" != "1" ]; then
  $PSQL -c "CREATE DATABASE shopdb OWNER shop"
  echo "   created database shopdb"
fi

docker compose up -d --build shop-api
for i in $(seq 1 60); do
  [ "$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q shop-api)")" = healthy ] && break; sleep 3
done
docker compose ps shop-api
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile >/dev/null 2>&1 || true
docker image prune -f >/dev/null
if [ "$GENERATED" = 1 ]; then
  echo "   admin login: $SHOP_ADMIN_EMAIL / $SHOP_ADMIN_PASSWORD   (stored in /opt/apps/.env)"
fi
REMOTE_SCRIPT

echo "==> Health"
curl -sf https://shop.onekampus.com/actuator/health && echo || echo "warning: https://shop.onekampus.com not reachable yet (DNS / certificate?)"
echo "==> Done: https://shop.onekampus.com"
