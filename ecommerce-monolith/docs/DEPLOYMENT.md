# Deployment — shared Lightsail apps-server

The shop is a React storefront (static files) plus one more container on the existing **apps-server** (Lightsail 2 GB, Mumbai,
`16.4.118.190`) next to Dabba and the job portal — **no extra cost**.

```mermaid
flowchart LR
    U((Visitors)) -->|HTTPS| caddy[Caddy<br/>auto TLS]
    subgraph apps-server[/opt/apps · docker compose "apps"/]
      caddy -->|dabba.onekampus.com| dabba[dabba-api + dabba-web]
      caddy -->|jobs / jobs-api| jobs[job-api + static web]
      caddy -->|shop.onekampus.com| web[React build<br/>static files /srv/shop]
      caddy -->|shop-api.onekampus.com| shop[shop-api<br/>640 MB limit · ~380 MB used]
      dabba --> pg[(Postgres 17<br/>dabba · shopdb)]
      shop --> pg
      dabba --> redis[(Redis)]
    end
```

## Memory budget (measured 2026-11-15)

| | MB |
|---|---|
| Server RAM | 1906 (+2 GB swap) |
| Existing containers (Dabba API + web, job API, Postgres, Redis, Caddy) | ~570 used |
| Available before the shop | ~837 |
| Shop (lean JVM flags, after load) | ~376 |
| **Available after the shop** | **~450** |

If all three apps ever get real traffic at the same time, resize the instance to 4 GB (Lightsail →
instance → snapshot → create a larger instance from it, ~USD 24/month); nothing else changes.

## Where the configuration lives

| File | Repo | Purpose |
|---|---|---|
| `deploy/compose.yml` | `dabba-platform` | the shared server's compose file — contains the `shop-api` service |
| `deploy/Caddyfile` | `dabba-platform` | all sites, including `shop.onekampus.com` (static React) and `shop-api.onekampus.com` (API) |
| `ecommerce-monolith/deploy/Dockerfile` | this repo | runtime-only image (jar built locally) |
| `ecommerce-monolith/deploy/deploy.sh` | this repo | API: build + upload + first-run setup + start; syncs the shared Caddyfile/compose |
| `ecommerce-frontend/deploy/deploy.sh` | this repo | storefront: `npm run build` (API URL from `.env.production`) + upload to `/opt/apps/sites/shop` |
| `/opt/apps/.env` | server only | secrets: `SHOP_DB_PASSWORD`, `SHOP_JWT_SECRET`, `SHOP_ADMIN_EMAIL`, `SHOP_ADMIN_PASSWORD`, optional `SHOP_DEMO_MODE` |

The dabba-platform repo is the single source of truth for the shared files; both apps' deploy
scripts sync them, so neither can remove the other's service.

## First deployment

1. **DNS** (GoDaddy): A records **`shop` → `16.4.118.190`** and **`shop-api` → `16.4.118.190`**.
2. Deploy the storefront files first (nothing serves them until Caddy is reloaded), then the API:
   ```bash
   cd Spring-Boot-Microservices
   ./ecommerce-frontend/deploy/deploy.sh
   ./ecommerce-monolith/deploy/deploy.sh      # also syncs the Caddyfile and reloads Caddy
   ```
   First run only: generates the four `SHOP_*` secrets into `/opt/apps/.env` and prints the admin
   login once, creates role `shop` and database `shopdb` in the shared Postgres.
3. Caddy obtains the Let's Encrypt certificates on the first HTTPS request to each domain.
4. Open https://shop.onekampus.com — demo customer `customer@shop.demo` / `Customer@123`;
   API docs at https://shop-api.onekampus.com/swagger-ui.html.

## Updates and rollback

```bash
./ecommerce-monolith/deploy/deploy.sh    # API: rebuild jar, upload, restart shop-api only
./ecommerce-frontend/deploy/deploy.sh    # storefront: rebuild and upload static files (no restart)
```
Rollback: check out the previous commit and run `deploy.sh` again. Flyway migrations are additive,
so an older jar keeps working with a newer schema (Hibernate `validate` only checks mapped columns).

## Operations

```bash
ssh -i ~/.ssh/apps-server-lightsail.pem ubuntu@16.4.118.190
cd /opt/apps
docker compose ps shop-api
docker compose logs -f --tail=200 shop-api
docker stats --no-stream
docker compose exec postgres pg_dump -U dabba -Fc shopdb > ~/shopdb-$(date +%F).dump   # backup
```

Turn the public demo off: set `SHOP_DEMO_MODE=false` in `/opt/apps/.env` and
`docker compose up -d shop-api` (existing demo data stays; no new seeding).
