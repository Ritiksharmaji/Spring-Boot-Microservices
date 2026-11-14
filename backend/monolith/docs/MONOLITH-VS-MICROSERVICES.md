# The same system, two architectures

This repository contains the same e-commerce system twice:

- **[`backend/microservices/`](../../microservices)** — 8 Spring Cloud microservices
- **[`backend/monolith/`](..)** — one Spring Boot application with Spring Modulith modules (live API https://shop-api.onekampus.com,
  used by the React storefront https://shop.onekampus.com from [`frontend/web/`](../../../frontend/web))

Same core features, same business rules, same REST paths. The monolith additionally serves the React
storefront's extras (sizes, multiple images, address book, wishlist, cash on delivery). What differs is *how the parts talk,
where consistency comes from, and what it costs to run*.

## Side by side

| Concern | Microservices (`backend/microservices`) | Modular monolith (`backend/monolith`) |
|---|---|---|
| Deployables | 8 services + gateway/config/discovery infrastructure | 1 jar |
| Module boundaries | process + network boundaries | packages, verified by `ApplicationModules.verify()` in a test |
| Data | database per service (3× PostgreSQL DBs + MongoDB) | one PostgreSQL database, tables owned per module |
| Identity | Keycloak (OIDC, PKCE), gateway validates JWT, sets `X-User-*` headers | self-issued HS256 JWT, Spring Security in the app |
| Module-to-module calls | HTTP (`@HttpExchange` + Eureka + load balancer) with Resilience4j | plain Java method calls on public interfaces |
| Events | RabbitMQ via Spring Cloud Stream, consumer groups, DLQs | in-process `ApplicationEvent`s + `@ApplicationModuleListener` |
| Reliable publishing | transactional **outbox** table + scheduled publisher | **event publication registry** (same idea, built into Spring Modulith) |
| Checkout consistency | stock reserved by remote call, order saved locally; compensation (release) if saving fails | stock reservation + order + event in **one ACID transaction** |
| Payment failure | saga compensation over messages | listener → `OrderPayments.paymentFailed` → cancel + release stock in one transaction |
| Failure isolation | a crashed service does not stop the others | one crash stops everything (restart policy + health checks) |
| Scaling | each service independently | the whole app (stateless, so replicas work) |
| Observability | distributed tracing (Zipkin), Prometheus, Loki, Grafana across services | one log stream, Actuator, Modulith actuator (`/actuator/modulith`) |
| Config | Config Server + Bus refresh | environment variables |
| Local run | `docker compose up` (15 containers, ~5 GB) | `./mvnw spring-boot:run` + PostgreSQL |
| Production footprint | ~6.7 GB of container limits → separate 8 GB server (~USD 44/month) | ~380 MB → shares an existing 2 GB server (USD 0 extra) |
| Production Java (non-blank, non-comment) | ~2,160 lines in 8 services, plus Compose/Keycloak/Prometheus/Grafana configuration | ~1,410 lines in 1 application |
| Tests | 49 + end-to-end smoke test on the full stack | 14 incl. async journey and PostgreSQL race test |

## What stayed exactly the same

- REST paths and payloads (`/api/products`, `/api/cart`, `/api/orders`, `/api/payments`, `/api/users/me`)
- Atomic stock reservation with `UPDATE … WHERE stock >= qty`
- Order state machine and its error codes (422 for invalid transitions)
- Idempotent payment per order, refund on cancel, "cancelled before payment → never charged"
- RFC 7807 error responses, validation rules, ownership checks returning 404

## What became simpler in the monolith

1. **No distributed transaction problem for checkout.** The order and the stock change commit together.
2. **No network failure modes between modules** — no retries, circuit breakers or timeouts needed inside the app.
3. **No infrastructure to run** — no gateway, Eureka, Config Server, broker, Keycloak.
4. **Refactoring across modules is a compile-time change**, not an API versioning exercise.

## What the microservices version still does better

1. **Independent deployment and scaling** per team / per hot spot (e.g. scale only the catalog).
2. **Fault isolation** — a memory leak in notifications cannot take checkout down.
3. **Technology freedom** (MongoDB for users) and **team autonomy** at larger scale.
4. **Enterprise identity** out of the box (Keycloak: SSO, social login, MFA, admin console).

## When to choose which

| Situation | Choose |
|---|---|
| One team, product still evolving, cost-sensitive | **Modular monolith** — boundaries without the operational cost |
| Several teams, independent release cadences, very uneven load per area | **Microservices** |
| Not sure yet | Start modular; a Modulith module with its own tables, a public API and events is already shaped like a service and can be extracted later |

## How a module would be extracted

The monolith's module structure maps 1:1 to the services:

| Monolith module | Becomes | Change needed |
|---|---|---|
| `catalog` | product-service | `Catalog` interface → HTTP client (`@HttpExchange`) |
| `payment` | payment-service | `@ApplicationModuleListener` → message consumer; `OrderPayments` call → `payment.result` event |
| `notification` | notification-service | listener → message consumer |
| `identity` | user-service + Keycloak | self-issued JWT → OIDC provider |
| event publication registry | transactional outbox + broker | Spring Modulith can externalize events to RabbitMQ/Kafka (`@Externalized`) |

That is exactly the path from `backend/monolith/` to `backend/microservices/` in this repository.
