# Architecture Decision Records

Short records of the important decisions: context, decision, consequences.

| # | Decision | Status |
|---|---|---|
| 001 | Microservices with database per service | accepted |
| 002 | API gateway as the single entry point (Spring Cloud Gateway) | accepted |
| 003 | Keycloak + JWT, roles CUSTOMER / ADMIN, identity headers set only by the gateway | accepted |
| 004 | Eureka service discovery with client-side load balancing | accepted |
| 005 | Centralized configuration (Spring Cloud Config, native) + Spring Cloud Bus | accepted |
| 006 | Synchronous stock reservation with an atomic conditional UPDATE | accepted |
| 007 | Checkout as a choreography saga | accepted |
| 008 | RabbitMQ instead of Kafka | accepted (supersedes the Kafka setup of branch-15-2) |
| 009 | Transactional outbox for order events | accepted |
| 010 | Idempotent consumers, at-least-once delivery, DLQs | accepted |
| 011 | Resilience4j: retries for reads only, circuit breakers everywhere, business errors ignored | accepted |
| 012 | RFC 7807 problem details for all errors | accepted |
| 013 | Polyglot persistence: MongoDB for users, PostgreSQL elsewhere | accepted |
| 014 | One Dockerfile for all modules, Compose for local and production | accepted |
| 015 | Single Lightsail instance for production | accepted (revisit at scale) |
| 016 | Testing pyramid with Testcontainers and an end-to-end smoke test in CI | accepted |

---

### ADR-001 Microservices with database per service
**Context:** the project's purpose is to demonstrate independently deployable services.
**Decision:** split by business capability (users, catalog, orders, payments, notifications);
each owns its database; no shared tables.
**Consequences:** + independent scaling/deploys, failure isolation, technology freedom.
− distributed consistency (solved by the saga, ADR-007), more operational components.

### ADR-002 API gateway
**Context:** clients should not know service locations; security must be enforced once.
**Decision:** Spring Cloud Gateway (reactive) routes `/api/**` via Eureka, validates JWTs, applies
roles, CORS, rate limiting and circuit breakers.
**Consequences:** + one public surface, consistent policies. − extra hop; the gateway must be
highly available (stateless → run replicas).

### ADR-003 Identity
**Context:** services need "who is calling" without each validating tokens and without trusting the client.
**Decision:** Keycloak issues JWTs; the gateway validates them and is the **only** component that
sets `X-User-ID`/`X-User-Roles`/`X-User-Email` (client values are stripped). Services trust these
headers because they are not reachable from outside (no published ports, `/internal` not routed).
Roles: `CUSTOMER` (default for sign-ups) and `ADMIN`.
**Consequences:** + simple services, no impersonation. − services must stay on the private
network; with a zero-trust requirement they would also validate the JWT themselves.

### ADR-004 Service discovery
**Decision:** Netflix Eureka + Spring Cloud LoadBalancer (`lb://name`, `@LoadBalanced RestClient`).
**Consequences:** + instances can come and go. In Kubernetes this would be replaced by K8s Services.

### ADR-005 Configuration
**Decision:** Config Server (native profile, files packaged under `config/`, overridable with
`CONFIG_REPO_LOCATION`); shared `application.yml` + `{service}.yml`; every host/secret is an
environment variable with a local default; Bus broadcasts refreshes.
**Consequences:** + one place for configuration, no secrets in git. − config server is a start-up dependency (services fall back to `optional:`).

### ADR-006 Stock reservation
**Context:** overselling is the worst correctness bug for a shop.
**Decision:** order-service reserves stock synchronously before creating the order using
`UPDATE … SET stock = stock - q WHERE id = ? AND active AND stock >= q` for every line in one
transaction (all-or-nothing).
**Consequences:** + no locks held across calls, no oversell under concurrency (proved by a
30-thread test on PostgreSQL), immediate feedback to the customer. − a reservation is not
time-limited yet (see future improvements).

### ADR-007 Choreography saga
**Context:** order, stock and payment live in different databases.
**Decision:** events drive the flow: `order.created` → payment → `payment.result` → order →
`order.status`. Compensations: release stock, refund.
**Consequences:** + loose coupling, no coordinator. − the flow is spread across services
(documented in APPLICATION-FLOW.md); with more steps an orchestrator would be clearer.

### ADR-008 RabbitMQ instead of Kafka
**Context:** both were explored in the learning branches. RabbitMQ is already required for
Spring Cloud Bus. Production runs on one 8 GB machine.
**Decision:** RabbitMQ binder for all business events.
**Consequences:** + one broker instead of two (~1 GB RAM saved), per-message ack, built-in DLQs.
− no long-term event log/replay; switching back to Kafka only means changing the binder dependency
and binder properties (Spring Cloud Stream abstracts the code).

### ADR-009 Transactional outbox
**Context:** "save order, then publish" loses the event if the broker is down or the process
dies in between; "publish, then save" can announce orders that do not exist.
**Decision:** write the event into `outbox_event` in the same transaction; a scheduled publisher
sends unpublished rows in order and marks them.
**Consequences:** + no lost or phantom events. − ~1 s latency; table needs purging (done nightly);
one publisher instance (multi-instance needs `SKIP LOCKED`).

### ADR-010 Idempotency and DLQs
**Decision:** assume at-least-once delivery. payment: unique `orderId`; order: only `PENDING`
accepts payment results; 3 attempts then dead-letter queue.
**Consequences:** duplicates are harmless; poison messages do not block queues.

### ADR-011 Resilience policy
**Decision:** reads (`getProduct`, `getUser`, stock release) retry 3× with exponential back-off;
stock **reservation is never retried** (not idempotent); every downstream has a circuit breaker;
`BusinessException`s (404, 409, 422) are ignored by breakers and retries.
**Consequences:** transient faults are absorbed, outages fail fast with 503, user errors are not
mistaken for outages.

### ADR-012 Error format
**Decision:** every service returns RFC 7807 `ProblemDetail` with a stable `title`; validation
adds an `errors` map. **Consequences:** clients handle errors uniformly.

### ADR-013 Polyglot persistence
**Decision:** MongoDB for the user profile (document with embedded address), PostgreSQL for
transactional data. **Consequences:** shows both; unique indexes (`email`, `keycloakId`) need
`auto-index-creation`.

### ADR-014 Containers
**Decision:** one multi-stage Dockerfile with a `MODULE` argument; layered jars; non-root;
container-aware heap. Compose for local (`docker-compose.yml`) and production (`deploy/docker-compose.prod.yml`).
**Consequences:** identical images in CI and production; the Compose files translate directly to ECS/Kubernetes later.

### ADR-015 Hosting
**Decision:** one AWS Lightsail 8 GB instance, Nginx + Let's Encrypt, images from GHCR.
**Consequences:** ~USD 40/month; single point of failure (mitigated by snapshots, backups and fast redeploy). Next step at scale: managed DB (RDS), Amazon MQ, ECS/EKS with multiple replicas.

### ADR-016 Testing strategy
**Decision:** unit tests for rules, Spring Boot integration tests with H2 and mocked HTTP clients
for flows, Testcontainers for real Postgres/Mongo/RabbitMQ behaviour, and a full-stack smoke test
in CI. **Consequences:** fast local feedback; infrastructure-specific bugs (e.g. `@Lob` on
PostgreSQL, queue declaration timing) are caught in CI before production.
