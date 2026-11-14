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

### 🌐 Live full-stack shop — React + Spring Boot modular monolith

| | Folder | Live |
|---|---|---|
| **Frontend** — React 18, Vite, Tailwind | [`ecommerce-frontend/`](ecommerce-frontend) | **https://shop.onekampus.com** |
| **Backend** — Spring Boot 3.5 + Spring Modulith, PostgreSQL | [`ecommerce-monolith/`](ecommerce-monolith) | **https://shop-api.onekampus.com** ([Swagger](https://shop-api.onekampus.com/swagger-ui.html)) |

Demo login `customer@shop.demo` / `Customer@123`. The backend is the same system as the
microservices, built as **one application with enforced module boundaries** (identity, catalog,
ordering, payment, notification, wishlist) and transactional domain events instead of a broker —
it runs in ~380 MB on a shared 2 GB server. Comparison:
[MONOLITH-VS-MICROSERVICES.md](ecommerce-monolith/docs/MONOLITH-VS-MICROSERVICES.md).

```
Spring-Boot-Microservices/
├── ecommerce-platform/    8 Spring Cloud microservices (local + CI)
├── ecommerce-monolith/    Spring Modulith backend (live API)
├── ecommerce-frontend/    React storefront (live)
└── MicroserverWithDiffrentServices/, ConfigurationManagment/   course notes (learning branches)
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

The `branch-19-*` series built the modular monolith:

| Step | Branch |
|---|---|
| 1. Modules, Flyway, JWT auth, catalog | `branch-19-1-monolith-foundation-modules-auth-catalog` |
| 2. Cart, orders, payments, notifications via Modulith events | `branch-19-2-cart-orders-payments-modulith-events` |
| 3. Storefront page, Docker, apps-server deploy kit, CI | `branch-19-3-docker-and-apps-server-deploy` |
| 4. Docs, comparison, Postman | `branch-19-4-docs-monolith-vs-microservices` |
| 5. Live deployment fixes | `branch-19-5-live-deploy-fixes` |

The `branch-20-*` series made it full-stack with React:

| Step | Branch |
|---|---|
| 1. Sizes, images, address book, wishlist, cash on delivery (backend) | `branch-20-1-backend-sizes-images-wishlist-addresses-cod` |
| 2. React storefront on the Spring Boot API | `branch-20-2-react-frontend-on-monolith-api` |
| 3. Deploy shop.onekampus.com (React) + shop-api.onekampus.com (API) | `branch-20-3-deploy-shop-frontend-and-shop-api` |
| 4. Documentation | `branch-20-4-docs-full-stack` |
