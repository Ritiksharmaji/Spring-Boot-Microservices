# Project Journey — from course branches to a production system

This repository is both a **learning log** and a **finished product**.

- `branch-2` … `branch-17-2` follow a Spring Boot microservices course step by step; they are
  kept unchanged as a record of how each concept was learned.
- `branch-18-*` turns the course code into a complete, production-ready platform
  (`ecommerce-platform/`), one focused branch per step, each merged into
  `branch-18-completing-ecommerce-microservices-production-ready` and finally into `main`.

## Learning branches (course)

| Branches | Topic |
|---|---|
| `main` (initial) | monolith split into user / product / order services, PostgreSQL, timezone fix |
| `branch-2*` | different databases — PostgreSQL, MongoDB |
| `branch_3`–`branch_5`, `branch-6*` | configuration management, Spring Cloud Config server/client |
| `branch-7*` | inter-service communication: RestTemplate, RestClient, WebClient, HTTP interfaces |
| `branch-8*` | service discovery with Eureka, load balancing, heartbeats |
| `branch-9*` | inter-service communication inside the e-commerce app (order → product/user) |
| `branch-10*` | observability: logs, metrics, tracing, Grafana, Zipkin, MySQL in Docker |
| `branch-11*` | API gateway: routing (Java vs YAML), global filters, Eureka integration, rewrite path |
| `branch-12*` | Resilience4j: circuit breaker, retry, rate limiting; MySQL → PostgreSQL |
| `branch-13*` | asynchronous communication with RabbitMQ, notification service, CloudAMQP |
| `branch-14*` | Apache Kafka: producers/consumers, (de)serialization, Spring Cloud Function/Stream |
| `branch-15*` | e-commerce app on Spring Cloud Stream (RabbitMQ, then Kafka) |
| `branch-16*` | OAuth2 + Keycloak: client credentials, PKCE |
| `branch-17*` | securing the e-commerce app with Keycloak, role-based authorization |

## Completion series

| Branch | What it added |
|---|---|
| `branch-18-1-cleanup-secrets-and-project-structure` | `ecommerce-platform/` with clean service names, parent POM, env-driven config, secrets removed, Linux-safe names |
| `branch-18-2-complete-business-logic-cart-order-stock` | real prices, atomic stock, checkout, order history/cancel/admin status, profile linked to Keycloak, identity propagation fix, RFC 7807 errors |
| `branch-18-3-payment-service-and-order-saga` | payment-service, choreography saga, transactional outbox, RabbitMQ with DLQs, email notifications |
| `branch-18-4-dockerize-all-services-full-compose` | Dockerfile, full Compose stack, Keycloak realm import, Prometheus/Loki/Grafana |
| `branch-18-5-testing-and-openapi-docs` | Testcontainers, slice tests, JaCoCo, OpenAPI + aggregated Swagger UI |
| `branch-18-6-ci-cd-github-actions` | CI with end-to-end smoke test, GHCR images, deploy workflow, CodeQL, Dependabot |
| `branch-18-7-deploy-aws-lightsail` | production compose, Nginx + TLS, prod realm, server/deploy/backup scripts, CORS |
| `branch-18-8-readme-architecture-docs` | system design, flows, API reference, events, ADRs, Postman collection, guides |

## Before → after

| Area | Course code (branch-17-2) | Platform (branch-18) |
|---|---|---|
| Prices / totals | hard-coded 1000 / 3000, total ignored quantity | live prices, correct totals, price snapshot |
| Stock | checked, never reduced | atomic reservation, released on failure/cancel |
| Identity | client-supplied `X-User-ID` trusted | taken from the verified JWT only |
| Order lifecycle | create only | PENDING → CONFIRMED/CANCELLED → SHIPPED → DELIVERED, cancel, history |
| Payments | none | payment-service with idempotency and refunds |
| Events | fire-and-forget Kafka publish | transactional outbox, RabbitMQ, DLQs, idempotent consumers |
| Config | Windows path, secrets in git | packaged config, env variables, `.env.example` |
| Errors | `null` / plain strings | RFC 7807 problem details, validation |
| Tests | 7 empty `contextLoads` | 49 automated tests incl. Testcontainers and an end-to-end smoke test |
| Run | IDE, local DBs, manual | `docker compose up`, CI/CD, production deploy kit |
| Docs | course notes | system design, flows, ADRs, API reference, runbooks |
