# Spring Boot Microservices — learning log → production platform

This repository has two parts:

### 🚀 [`ecommerce-platform/`](ecommerce-platform) — the finished system

A production-style e-commerce backend with **8 Spring Boot microservices**: API gateway with
Keycloak OAuth2, choreography saga with a transactional outbox over RabbitMQ, Resilience4j,
Prometheus/Grafana/Loki/Zipkin, Testcontainers and end-to-end CI, Docker and AWS Lightsail
deployment. **Start here → [ecommerce-platform/README.md](ecommerce-platform/README.md)**

```bash
cd ecommerce-platform && docker compose up -d --build
```

### 📚 Learning branches — how it was built

`branch-2` … `branch-17-2` follow a Spring Boot microservices course concept by concept
(configuration, service discovery, inter-service communication, API gateway, Resilience4j,
RabbitMQ, Kafka, Keycloak). The course notes and screenshots live in
[`MicroserverWithDiffrentServices/`](MicroserverWithDiffrentServices) and
[`ConfigurationManagment/`](ConfigurationManagment).

The `branch-18-*` series turned that code into the platform above; see
[docs/PROJECT-JOURNEY.md](ecommerce-platform/docs/PROJECT-JOURNEY.md) for the full map.

| Completion step | Branch |
|---|---|
| 1. Clean structure, secrets removed | `branch-18-1-cleanup-secrets-and-project-structure` |
| 2. Business logic: cart, orders, stock | `branch-18-2-complete-business-logic-cart-order-stock` |
| 3. Payment service, saga, outbox | `branch-18-3-payment-service-and-order-saga` |
| 4. Docker & full compose stack | `branch-18-4-dockerize-all-services-full-compose` |
| 5. Testing & OpenAPI | `branch-18-5-testing-and-openapi-docs` |
| 6. CI/CD | `branch-18-6-ci-cd-github-actions` |
| 7. AWS Lightsail deployment kit | `branch-18-7-deploy-aws-lightsail` |
| 8. Documentation | `branch-18-8-readme-architecture-docs` |
