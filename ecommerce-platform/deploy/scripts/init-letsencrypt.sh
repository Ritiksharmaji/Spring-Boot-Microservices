#!/usr/bin/env bash
# Obtains the first Let's Encrypt certificate (one cert covering API_DOMAIN and AUTH_DOMAIN)
# and installs a renewal cron job. DNS A records must already point to this server.
set -euo pipefail
cd "$(dirname "$0")/.."
set -a; source .env.prod; set +a
COMPOSE="docker compose -f docker-compose.prod.yml --env-file .env.prod"
STAGING=${STAGING:-0}   # STAGING=1 to test against the Let's Encrypt staging CA (no rate limits)

for d in "$API_DOMAIN" "$AUTH_DOMAIN"; do
  ip=$(getent hosts "$d" | awk '{print $1}' | head -1 || true)
  echo "DNS $d -> ${ip:-<not resolved>}"
done

echo "==> Requesting certificate (standalone, port 80 must be free)"
$COMPOSE stop nginx 2>/dev/null || true
$COMPOSE run --rm -p 80:80 --entrypoint certbot certbot certonly --standalone \
  $( [ "$STAGING" = 1 ] && echo --staging ) \
  --non-interactive --agree-tos -m "$LETSENCRYPT_EMAIL" \
  -d "$API_DOMAIN" -d "$AUTH_DOMAIN"

echo "==> Installing renewal cron (twice a day, webroot through the running Nginx)"
DEPLOY_DIR=$(pwd)
CRON="17 3,15 * * * cd $DEPLOY_DIR && $COMPOSE run --rm certbot renew --webroot -w /var/www/certbot --quiet && $COMPOSE exec -T nginx nginx -s reload >> $HOME/certbot-renew.log 2>&1"
( crontab -l 2>/dev/null | grep -v 'certbot renew' ; echo "$CRON" ) | crontab -
echo "Done. Start the stack with ./scripts/deploy.sh"
