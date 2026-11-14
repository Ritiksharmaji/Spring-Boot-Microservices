# API Gateway (`api-gateway`, port 8080)

Single public entry point (Spring Cloud Gateway, WebFlux): JWT validation against Keycloak, role rules, identity propagation (`X-User-*` headers), CORS, Redis rate limiting, circuit breakers with fallbacks, routing through Eureka, aggregated Swagger UI.

Key classes: `security/SecurityConfig` (rules + role mapping), `security/UserContextFilter` (identity headers), `GatewayConfig` (routes, rate limiter), `FallbackController`.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| * | `/api/users/**, /api/products/**, /api/cart/**, /api/orders/**, /api/payments/**` | routed to the services |
| GET | `/swagger-ui.html` | API docs of all services |
| GET | `/actuator/health` | public health |
| * | `/fallback/*` | circuit-breaker fallbacks (503) |
| GET | `/eureka/main` | Eureka dashboard (ADMIN) |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

Redis (rate-limiter buckets).

## Events

— See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

Keycloak (JWKS), Redis, Eureka

## Configuration

`KEYCLOAK_ISSUER_URI`, `KEYCLOAK_JWK_SET_URI`, `CORS_ALLOWED_ORIGINS`, `REDIS_*`, `FORWARD_HEADERS_STRATEGY`, `API_DOCS_ENABLED` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from ecommerce-platform/
./mvnw -pl api-gateway -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl api-gateway test                       # tests: `GatewaySecurityTest`, `GatewayCorsTest`, `UserContextFilterTest`
docker compose up -d --build api-gateway          # rebuild just this container
```
