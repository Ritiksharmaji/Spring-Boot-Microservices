#!/usr/bin/env bash
# =============================================================================
# Build the React storefront with the production API URL (.env.production) and upload it to
# the shared Lightsail apps-server, where Caddy serves it at https://shop.onekampus.com.
#
#   ecommerce-frontend/deploy/deploy.sh
#
# Static files only: no container, no restart. The Caddy site (in dabba-platform/deploy/Caddyfile)
# is synced by ecommerce-monolith/deploy/deploy.sh.
# Env overrides: DEPLOY_HOST (ubuntu@16.4.118.190), SSH_KEY (~/.ssh/apps-server-lightsail.pem)
# =============================================================================
set -euo pipefail
HOST="${DEPLOY_HOST:-ubuntu@16.4.118.190}"
KEY="${SSH_KEY:-$HOME/.ssh/apps-server-lightsail.pem}"
REMOTE=/opt/apps/sites/shop
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> Building ($(grep VITE_BACKEND_URL "$APP_DIR/.env.production"))"
(cd "$APP_DIR" && npm ci --no-audit --no-fund --silent && npm run build --silent)

echo "==> Uploading dist/ to $REMOTE"
ssh -i "$KEY" "$HOST" "mkdir -p $REMOTE"
rsync -az --delete -e "ssh -i $KEY" "$APP_DIR/dist/" "$HOST:$REMOTE/"

echo "==> Check"
curl -sf -o /dev/null -w "https://shop.onekampus.com -> %{http_code}\n" https://shop.onekampus.com/ || echo "warning: storefront not reachable yet"
echo "==> Done: https://shop.onekampus.com"
