#!/usr/bin/env bash
# Nightly backup of all databases (PostgreSQL: products, orders, payments, Keycloak; MongoDB: users).
# Keeps BACKUP_RETENTION_DAYS days locally and optionally uploads to S3.
#   crontab:  30 2 * * * /home/ubuntu/ecommerce/backend/microservices/deploy/scripts/backup.sh >> /home/ubuntu/backup.log 2>&1
set -euo pipefail
cd "$(dirname "$0")/.."
set -a; source .env.prod; set +a
COMPOSE="docker compose -f docker-compose.prod.yml --env-file .env.prod"
STAMP=$(date +%Y%m%d-%H%M%S)
DIR=${BACKUP_DIR:-$HOME/backups}/$STAMP
mkdir -p "$DIR"

echo "==> PostgreSQL"
for db in productdb orderdb paymentdb keycloakdb; do
  $COMPOSE exec -T postgres pg_dump -U "$POSTGRES_USER" -Fc "$db" > "$DIR/$db.dump"
done

echo "==> MongoDB"
$COMPOSE exec -T mongodb mongodump --quiet --archive --gzip \
  -u "$MONGO_ROOT_USER" -p "$MONGO_ROOT_PASSWORD" --authenticationDatabase admin --db userdb > "$DIR/userdb.archive.gz"

tar -czf "$DIR.tar.gz" -C "$(dirname "$DIR")" "$STAMP" && rm -rf "$DIR"
echo "Backup written: $DIR.tar.gz ($(du -h "$DIR.tar.gz" | cut -f1))"

if [ -n "${BACKUP_S3_BUCKET:-}" ]; then
  aws s3 cp "$DIR.tar.gz" "s3://$BACKUP_S3_BUCKET/ecommerce/" --only-show-errors && echo "Uploaded to s3://$BACKUP_S3_BUCKET/ecommerce/"
fi

find "$(dirname "$DIR")" -name '*.tar.gz' -mtime +"${BACKUP_RETENTION_DAYS:-7}" -delete
