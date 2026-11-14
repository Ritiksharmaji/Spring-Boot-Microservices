#!/usr/bin/env bash
# =============================================================================
# Build the e-commerce monolith locally and (re)start it on the shared Lightsail
# apps-server (next to Dabba and the job portal), served at https://shop-api.onekampus.com.
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
grep -q "shop-api.onekampus.com" "$SHARED/Caddyfile" || { echo "shop-api site missing in $SHARED/Caddyfile"; exit 1; }

echo "==> Building jar"
(cd "$APP_DIR" && JAVA_HOME="${DEPLOY_JAVA_HOME:-$(/usr/libexec/java_home -v 21)}" ./mvnw -B -q -DskipTests package)
JAR=$(ls "$APP_DIR"/target/ecommerce-monolith-*.jar | grep -v original | head -1)

echo "==> Shared server files (compose.yml, Caddyfile) from $SHARED"
rsync -az --inplace -e "ssh -i $KEY" "$SHARED/compose.yml" "$SHARED/Caddyfile" "$HOST:$REMOTE/"

echo "==> Uploading jar"
"${SSH[@]}" "mkdir -p $REMOTE/shop-api"
scp -q -i "$KEY" "$APP_DIR/deploy/Dockerfile" "$HOST:$REMOTE/shop-api/Dockerfile"
scp -q -i "$KEY" "$JAR" "$HOST:$REMOTE/shop-api/shop.jar"

echo "==> Secrets, database, start"
scp -q -i "$KEY" "$APP_DIR/deploy/remote-setup.sh" "$HOST:$REMOTE/shop-api/remote-setup.sh"
"${SSH[@]}" "bash $REMOTE/shop-api/remote-setup.sh"

echo "==> Health"
curl -sf https://shop-api.onekampus.com/actuator/health && echo || echo "warning: https://shop-api.onekampus.com not reachable yet (DNS / certificate?)"
echo "==> Done: API https://shop-api.onekampus.com (storefront: ecommerce-frontend/deploy/deploy.sh)"
