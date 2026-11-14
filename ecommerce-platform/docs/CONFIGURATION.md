# Configuration

## Where configuration lives

```
config-server/src/main/resources/config/
├── application.yml            # shared by every config client (Eureka, RabbitMQ, actuator, tracing, logging, springdoc)
├── api-gateway.yml            # Redis, Keycloak, CORS, Swagger UI, breakers
├── user-service.yml           # MongoDB
├── product-service.yml        # PostgreSQL productdb
├── order-service.yml          # PostgreSQL orderdb, saga bindings, outbox, Resilience4j
├── payment-service.yml        # PostgreSQL paymentdb, bindings, simulated provider limit
└── notification-service.yml   # bindings, SMTP
```

Each service's own `application.yml` only names the service and imports
`configserver:${CONFIG_SERVER_URL}`. discovery-server and config-server are configured locally.

Precedence: environment variables > `{service}.yml` > shared `application.yml` > defaults in `${VAR:default}`.

**Refresh without restart:** change the file (mount a folder with `CONFIG_REPO_LOCATION=file:/config/`)
and `POST http://config-server:8888/actuator/busrefresh` — `@RefreshScope` beans reload.

## Environment variables

| Variable | Default (local) | Used by | Purpose |
|---|---|---|---|
| `CONFIG_SERVER_URL` | `http://localhost:8888` | all clients | config server |
| `CONFIG_REPO_LOCATION` | `classpath:/config` | config-server | folder with config files |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | all | registry |
| `EUREKA_HOSTNAME` | `localhost` | discovery | instance host name |
| `EUREKA_DASHBOARD_URL` | `http://localhost:8761` | gateway | `/eureka/**` route |
| `SERVER_PORT` | 8080–8085, 8761, 8888 | all | HTTP port |
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | product, order, payment | PostgreSQL |
| `DB_NAME` | `productdb` / `orderdb` / `paymentdb` | per service | database |
| `DB_USERNAME` / `DB_PASSWORD` | `ecom` / `ecom` | product, order, payment | credentials |
| `JPA_SHOW_SQL` | `false` | JPA services | log SQL |
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/userdb` | user | MongoDB |
| `SPRING_DATA_MONGODB_DATABASE` | `userdb` | user | database |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | all except discovery | broker |
| `RABBIT_USERNAME` / `RABBIT_PASSWORD` | `guest` / `guest` | | credentials |
| `RABBITMQ_VHOST` / `RABBITMQ_SSL` | `/` / `false` | | CloudAMQP: your vhost / `true` |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | gateway | rate limiter |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8483/realms/ecom-app` | gateway | expected `iss` |
| `KEYCLOAK_JWK_SET_URI` | `…/protocol/openid-connect/certs` | gateway | signing keys (internal URL in Docker) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | gateway | browser origins |
| `FORWARD_HEADERS_STRATEGY` | `framework` | gateway | trust X-Forwarded-* (behind Nginx) |
| `API_DOCS_ENABLED` | `true` | all | Swagger UI / OpenAPI |
| `PAYMENT_MAX_AMOUNT` | `50000.00` | payment | simulated decline threshold |
| `MAIL_HOST` … `MAIL_FROM` | empty = log only | notification | SMTP |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | all | traces |
| `TRACING_SAMPLING_PROBABILITY` | `1.0` (prod 0.1) | all | sampling |
| `EUREKA_SELF_PRESERVATION` | `false` | discovery | single-node registry |

Docker Compose-only variables (credentials of the infrastructure containers):
`POSTGRES_USER`, `POSTGRES_PASSWORD`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `KEYCLOAK_ADMIN`,
`KEYCLOAK_ADMIN_PASSWORD`, `GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD`; production adds
`MONGO_ROOT_*`, `API_DOMAIN`, `AUTH_DOMAIN`, `FRONTEND_URL`, `GHCR_*`, `IMAGE_TAG`, backup settings
(see `deploy/.env.prod.example`).

## Ports

| Port | Component | Published locally |
|---|---|---|
| 8080 | api-gateway | ✅ |
| 8081–8085 | user, product, order, notification, payment | ❌ (internal) |
| 8761 | Eureka | ✅ |
| 8888 | config-server | ❌ |
| 8483 | Keycloak | ✅ |
| 5432 / 27017 | PostgreSQL / MongoDB | ✅ (for IDE use) |
| 5672 / 15672 | RabbitMQ / management UI | ✅ |
| 9411 | Zipkin | ✅ |
| 8025 / 1025 | Mailpit UI / SMTP | ✅ |
| 3001 / 9090 | Grafana / Prometheus (observability profile) | ✅ |
