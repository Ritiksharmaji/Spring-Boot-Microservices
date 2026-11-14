# Resume & Interview Guide

## One-line summary

> Production-grade e-commerce backend with **8 Spring Boot microservices** — API gateway with
> Keycloak OAuth2, choreography **saga with a transactional outbox over RabbitMQ**, Resilience4j,
> full observability, **Testcontainers + end-to-end CI**, Dockerized and deployable to AWS.

## Resume bullets (pick 3–5)

**Java / Backend version**
- Built an e-commerce platform of **8 Spring Boot 3 / Spring Cloud microservices** (gateway, config,
  discovery, user, product, order, payment, notification) with database-per-service (PostgreSQL, MongoDB).
- Implemented checkout as a **choreography saga** over **RabbitMQ** with a **transactional outbox**,
  **idempotent consumers** and **dead-letter queues**, guaranteeing no lost events and no double charges.
- Prevented overselling with an **atomic conditional stock reservation**, verified by a
  **30-thread concurrency test on PostgreSQL (Testcontainers)**.
- Secured the system with **Keycloak (OAuth2/OIDC, PKCE)** at a **Spring Cloud Gateway**: JWT
  validation, role-based access, **identity propagation that blocks user impersonation**, Redis rate limiting, CORS.
- Added fault tolerance with **Resilience4j** (circuit breakers, retries with back-off, fallbacks)
  that distinguishes business errors from outages.
- Delivered observability with **Micrometer, Prometheus, Grafana, Loki and Zipkin** (metrics, logs and
  distributed traces correlated by trace id).
- Containerized all services (multi-stage **Docker**, layered jars, non-root) with a one-command
  **Docker Compose** stack; **GitHub Actions** CI runs unit, integration, Testcontainers and a full-stack
  **end-to-end smoke test**, publishes images to GHCR and deploys to **AWS Lightsail** behind Nginx + Let's Encrypt.

**Modular monolith bullets** (live link):
- Rebuilt the same platform as a **Spring Modulith modular monolith** (5 modules, boundaries verified
  in CI, transactional domain events with an event publication registry) — **live at
  https://shop.onekampus.com**, running in ~380 MB alongside two other apps on a shared 2 GB server.
- Documented the **monolith vs microservices trade-offs** of the same system (one ACID checkout vs saga +
  outbox, ~USD 0 vs ~USD 44/month hosting, deployment and failure-isolation differences).

**Full-stack version** (after adding the React frontend): replace the last bullet with
- Built a **React** storefront and admin panel consuming the microservices through the gateway
  with Keycloak PKCE login.

**Skills line:** Java 21, Spring Boot 3.5, Spring Cloud 2025 (Gateway, Config, Eureka, Stream, Bus),
Spring Security OAuth2, Keycloak, JPA/Hibernate, PostgreSQL, MongoDB, RabbitMQ, Redis, Resilience4j,
Docker, Docker Compose, GitHub Actions, Testcontainers, JUnit 5, Mockito, Prometheus, Grafana, Loki,
Zipkin, OpenAPI, Nginx, AWS Lightsail.

## 60-second walkthrough

"A customer logs in through Keycloak and calls the API gateway. The gateway validates the JWT,
checks roles and replaces any identity header with the token's subject, so services can't be fooled.
When they check out, order-service re-reads current prices, reserves stock in product-service with a
single conditional UPDATE so two buyers can never get the last item, then saves the order as
PENDING together with an outbox event in the same transaction. A publisher sends that event to
RabbitMQ; payment-service charges the order exactly once thanks to a unique key and replies with
the result; order-service confirms it — or cancels it and releases stock — and notification-service
emails the customer. Everything is observable in Grafana and Zipkin, tested with Testcontainers and an
end-to-end run in CI, and deployed with Docker to AWS."

## Likely interview questions

| Question | Short answer (details in the docs) |
|---|---|
| Why microservices here? | independent deploy/scale per capability, failure isolation; costs: distributed consistency + ops — handled with saga, outbox, observability |
| How do you keep data consistent across services? | choreography saga with compensations; local transactions + events; see SYSTEM-DESIGN §6 |
| What is the outbox pattern and why? | event stored in the same DB transaction, published later → no lost/phantom events (dual-write problem) |
| What if a message is delivered twice? | idempotent consumers: unique orderId in payments; state check (only PENDING) in orders |
| What if payment-service is down? | order stays PENDING, event waits in the durable queue; processed later |
| How do you prevent overselling? | `UPDATE … WHERE stock >= qty`, all lines in one transaction, no read-modify-write race |
| Why not retry the stock reservation? | not idempotent — a lost response + retry would reserve twice; reads are retried |
| Circuit breaker vs retry? | retry absorbs transient faults; breaker stops calling a failing service and fails fast; business 4xx ignored by both |
| How does auth work end to end? | PKCE login → JWT → gateway validates via JWKS → roles → `X-User-*` headers → services |
| How do you stop a user calling `/internal` APIs? | not routed by the gateway, blocked at Nginx, services not exposed outside the Docker network |
| RabbitMQ vs Kafka? | RabbitMQ: already used for Bus, lighter, per-message ack + DLQ; Kafka for replay/high throughput — code is binder-agnostic (Spring Cloud Stream) |
| How do you trace a request? | Micrometer Tracing propagates trace ids over HTTP and AMQP; Zipkin; logs carry the trace id → Loki/Grafana |
| How do you test it? | pyramid: unit → integration (H2, mocked clients) → Testcontainers (real Postgres/Mongo/RabbitMQ) → full-stack smoke test in CI |
| How is it deployed? | GitHub Actions builds images → GHCR → SSH deploy to Lightsail, Compose with health checks, Nginx TLS, rollback by image tag |
| Why build it twice (monolith and microservices)? | to compare: the monolith gets one ACID transaction for checkout and costs nothing to host; microservices buy independent deploy/scale and fault isolation at the price of sagas, outbox, brokers and infrastructure — see MONOLITH-VS-MICROSERVICES.md |
| How does Spring Modulith keep a monolith modular? | each top-level package is a module; only its top-level types are API; `ApplicationModules.verify()` in a test fails the build on internal access or cycles; modules talk through interfaces and events |
| What would you improve? | Flyway, reservation expiry, real payment provider + webhooks, outbox SKIP LOCKED for replicas, Kubernetes/HPA, read cache |
