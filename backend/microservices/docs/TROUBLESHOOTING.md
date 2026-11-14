# Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `docker compose up` very slow / containers OOM-killed | Docker Desktop has < 6 GB RAM | Settings → Resources → Memory 8 GB; skip the observability profile |
| A service stays `(health: starting)` | waiting for config-server / Eureka / DB | `docker compose logs <service>`; dependencies start first, give it 1–2 min |
| Every API call returns **401** | token expired (15 min) or `iss` mismatch | get a new token from `http://localhost:8483/...` (not `keycloak:8080`); tokens must come from the URL in `KEYCLOAK_ISSUER_URI` |
| **403** on cart/orders | user lacks `CUSTOMER` role | Keycloak admin → realm `ecom-app` → user → role mapping |
| `422 Profile Incomplete` at checkout | no profile/address | `PUT /api/users/me` with an address |
| `409 Insufficient Stock` | stock lower than cart quantity | reduce quantity / admin increases stock |
| `503 Service Unavailable` | downstream not registered yet or down; breaker open | `docker compose ps`; Eureka dashboard; wait 10 s for the breaker to half-open |
| 503 right after start-up | instances not yet in the clients' registry cache (refreshed every 5 s) | retry after ~10 s |
| Order stays **PENDING** | payment-service down or RabbitMQ unreachable | `docker compose logs payment-service order-service`; RabbitMQ UI queue `order.created.payment-service`; `select * from outbox_event where published_at is null` |
| Messages in a `*.dlq` queue | consumer threw 3 times | read the `x-exception-message` header in the RabbitMQ UI, fix, move messages back |
| No emails in Mailpit | `MAIL_HOST` not set for notification-service | compose sets it to `mailpit`; IDE run: `MAIL_HOST=localhost` |
| `Connection refused localhost:8888` in a service | config-server not running (IDE mode) | start config-server first (import is `optional:` so the service starts with defaults) |
| `FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"` | old JVM time-zone id with PostgreSQL | run with `-Duser.timezone=Asia/Kolkata` (the Docker images set `TZ=Asia/Kolkata`) |
| `password authentication failed for user` | old Postgres volume with different credentials | `docker compose down -v` (wipes data) or use the original credentials |
| Keycloak realm changes not applied | realm import runs only if the realm does not exist | delete the realm in the admin console or `docker compose down -v` |
| Port already in use | another local service on 8080/5432/… | stop it or change the published port in `docker-compose.yml` |
| Testcontainers tests "skipped" | Docker not running | start Docker; they always run in CI |
| CORS error in the browser | frontend origin not allowed | add it to `CORS_ALLOWED_ORIGINS` (gateway) and to the Keycloak client's web origins |
