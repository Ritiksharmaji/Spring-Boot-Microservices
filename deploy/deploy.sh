#!/usr/bin/env bash
# =============================================================================
# One entry point for everything that is deployed from this repository.
#
#   ./deploy/deploy.sh web    # React storefront  -> https://shop.onekampus.com      (frontend/web)
#   ./deploy/deploy.sh api    # Spring Boot API   -> https://shop-api.onekampus.com  (backend/monolith)
#   ./deploy/deploy.sh all    # both: static files first, then the API (which reloads Caddy)
#
# Target: the shared AWS Lightsail "apps-server" (also runs Dabba and the job portal).
# The microservices version (backend/microservices) is not deployed; it has its own
# production kit in backend/microservices/deploy for a separate 8 GB server.
# =============================================================================
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WHAT="${1:-}"

case "$WHAT" in
  web) "$ROOT/frontend/web/deploy/deploy.sh" ;;
  api) "$ROOT/backend/monolith/deploy/deploy.sh" ;;
  all)
    "$ROOT/frontend/web/deploy/deploy.sh"
    "$ROOT/backend/monolith/deploy/deploy.sh"
    ;;
  *)
    sed -n '3,12p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
