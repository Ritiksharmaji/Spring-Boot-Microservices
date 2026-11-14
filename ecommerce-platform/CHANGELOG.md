# Changelog

All notable changes of the production platform (`ecommerce-platform/`).

## [1.0.0] — 2026-11-15

First complete release, built on top of the course code of `branch-17-2`.

### Structure & configuration — `branch-18-1`
- Services moved to `ecommerce-platform/` with clear names; parent POM aligning Spring Boot 3.5.9 / Spring Cloud 2025.0.1 / Java 21.
- Config server serves packaged configuration; shared `application.yml`; every host/secret from environment variables; `.env.example`.
- Lower-case service names (config lookup works on Linux); committed `.env` files and logs untracked; leaked password redacted.

### Business features — `branch-18-2`
- Typed, validated product API with paging, category filter, search, soft delete; atomic stock reservation/release.
- Cart with live prices and stock checks; checkout with fresh prices, profile check, shipping-address snapshot, compensation.
- Order history, details, customer cancel, admin status changes with a state machine.
- User profile bound to the Keycloak account; unique email; role not self-assignable.
- Gateway sets identity headers from the JWT and strips client-supplied ones (impersonation fix); CUSTOMER/ADMIN role model.
- RFC 7807 errors everywhere; Resilience4j retries/breakers that ignore business errors.

### Saga & payments — `branch-18-3`
- New payment-service (idempotent charges, refunds, cancelled-before-payment guard).
- Choreography saga PENDING → CONFIRMED / CANCELLED; transactional outbox; RabbitMQ with DLQs and required groups (replacing Kafka).
- Notification-service sends order emails.

### Containers & observability — `branch-18-4`
- Multi-stage Dockerfile for all services; full Docker Compose stack with health-checked start-up.
- Keycloak realm import with roles and test users; Prometheus, Loki, Grafana dashboard, Zipkin.

### Tests & API docs — `branch-18-5`
- Testcontainers (PostgreSQL concurrency, MongoDB, RabbitMQ), controller slice tests, JaCoCo.
- OpenAPI for every service, aggregated Swagger UI at the gateway.

### CI/CD — `branch-18-6`
- GitHub Actions: build + tests, full-stack end-to-end smoke test, on-demand GHCR images, SSH deploy, CodeQL, Dependabot.
- Fixes found by CI: Mongo auditing in slices, RabbitMQ queue timing in tests, 5 s service-discovery refresh, no edge retries,
  matching DLQ declarations for producers/consumers, SMTP sender selection.

### Production — `branch-18-7`
- Lightsail deployment kit: production compose, Nginx + Let's Encrypt, production realm, setup/deploy/backup/restore scripts.
- Gateway CORS and forwarded-header handling.

### Documentation — `branch-18-8`
- System design, application flows, step-by-step guide, API reference, events, ADRs, security, observability,
  testing, configuration, deployment, troubleshooting, project journey, resume guide, per-service READMEs,
  Postman collection (52 requests, 74 assertions).
