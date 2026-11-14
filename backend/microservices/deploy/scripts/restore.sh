#!/usr/bin/env bash
# Restores a backup created by backup.sh.   ./scripts/restore.sh ~/backups/20261115-023000.tar.gz
# Stops the application services (databases keep running), restores, then starts everything again.
set -euo pipefail
cd "$(dirname "$0")/.."
ARCHIVE=${1:?usage: restore.sh <backup.tar.gz>}
set -a; source .env.prod; set +a
COMPOSE="docker compose -f docker-compose.prod.yml --env-file .env.prod"
read -r -p "This OVERWRITES current data with $ARCHIVE. Type 'restore' to continue: " ok
[ "$ok" = "restore" ] || { echo "aborted"; exit 1; }

TMP=$(mktemp -d); tar -xzf "$ARCHIVE" -C "$TMP"; SRC=$(find "$TMP" -mindepth 1 -maxdepth 1 -type d | head -1)
$COMPOSE stop api-gateway user-service product-service order-service payment-service notification-service keycloak

for db in productdb orderdb paymentdb keycloakdb; do
  [ -f "$SRC/$db.dump" ] || continue
  echo "==> $db"
  $COMPOSE exec -T postgres pg_restore -U "$POSTGRES_USER" -d "$db" --clean --if-exists < "$SRC/$db.dump"
done
echo "==> userdb"
$COMPOSE exec -T mongodb mongorestore --quiet --archive --gzip --drop \
  -u "$MONGO_ROOT_USER" -p "$MONGO_ROOT_PASSWORD" --authenticationDatabase admin < "$SRC/userdb.archive.gz"

rm -rf "$TMP"
$COMPOSE up -d --wait
echo "Restore complete."
