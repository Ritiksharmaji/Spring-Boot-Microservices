# oneKampus Shop — full-stack e-commerce (Spring Boot · React)

[![CI](https://github.com/Ritiksharmaji/Spring-Boot-Microservices/actions/workflows/ci.yml/badge.svg)](https://github.com/Ritiksharmaji/Spring-Boot-Microservices/actions/workflows/ci.yml)
[![Monolith CI](https://github.com/Ritiksharmaji/Spring-Boot-Microservices/actions/workflows/monolith.yml/badge.svg)](https://github.com/Ritiksharmaji/Spring-Boot-Microservices/actions/workflows/monolith.yml)
[![Frontend CI](https://github.com/Ritiksharmaji/Spring-Boot-Microservices/actions/workflows/frontend.yml/badge.svg)](https://github.com/Ritiksharmaji/Spring-Boot-Microservices/actions/workflows/frontend.yml)

Everything for the e-commerce system lives in this repository: web and mobile frontends, and the
backend built two ways — as a **modular monolith** (live) and as **Spring Cloud microservices**.

**Live:** shop **https://shop.onekampus.com** · API **https://shop-api.onekampus.com**
([Swagger](https://shop-api.onekampus.com/swagger-ui.html)) · demo login `customer@shop.demo` / `Customer@123`

## Repository structure

```
├── frontend/
│   ├── web/              React 18 · Vite · Tailwind storefront            → shop.onekampus.com
│   └── mobile/           React Native app: cli/ (React Native CLI) + expo/ (earlier version)
├── backend/
│   ├── monolith/         Spring Boot 3.5 · Spring Modulith · PostgreSQL    → shop-api.onekampus.com
│   └── microservices/    8 Spring Cloud services · Keycloak · RabbitMQ saga (local + CI)
├── deploy/               one entry point: ./deploy/deploy.sh web | api | all
├── learning/             course notes, screenshots and practice projects (learning branches)
└── .github/workflows/    CI per project (paths-filtered) · CodeQL · deploy
```

| Folder | What | Run locally | Docs |
|---|---|---|---|
| [`frontend/web`](frontend/web) | React storefront: catalog, sizes, cart, wishlist, address book, checkout (card / cash on delivery), orders | `npm ci && npm run dev` | [README](frontend/web/README.md) |
| [`frontend/mobile`](frontend/mobile) | React Native app (Android / iOS) — `cli/` (React Native CLI 0.81) and `expo/` (Expo SDK 54); still on the Express API | `cd cli && npm ci && npm start` | [README](frontend/mobile/README.md) |
| [`backend/monolith`](backend/monolith) | one Spring Boot app, 6 verified modules, transactional events, Flyway | `./mvnw spring-boot:run` | [README](backend/monolith/README.md) · [architecture](backend/monolith/docs/ARCHITECTURE.md) · [API](backend/monolith/docs/API.md) |
| [`backend/microservices`](backend/microservices) | gateway, config, discovery, user, product, order, payment, notification; saga + outbox; observability | `docker compose up -d --build` | [README](backend/microservices/README.md) · [system design](backend/microservices/docs/SYSTEM-DESIGN.md) |
| [`deploy`](deploy) | deployment of web + API to AWS Lightsail | `./deploy/deploy.sh all` | [README](deploy/README.md) |

**Monolith or microservices?** Same business rules, same REST paths — compared in
[MONOLITH-VS-MICROSERVICES.md](backend/monolith/docs/MONOLITH-VS-MICROSERVICES.md).

## CI

| Workflow | Runs when | Checks |
|---|---|---|
| Frontend CI | `frontend/web/**` changes | production build |
| Monolith CI | `backend/monolith/**` changes | module boundaries, tests (H2 + PostgreSQL Testcontainers), Docker image |
| CI | `backend/microservices/**` changes | unit/integration/Testcontainers tests, full Docker Compose end-to-end smoke test |
| CodeQL | weekly + microservices changes | security analysis |

## How it was built

`branch-2` … `branch-17-2` follow a Spring Boot microservices course (configuration, discovery,
inter-service communication, API gateway, Resilience4j, RabbitMQ, Kafka, Keycloak); their notes are in
[`learning/`](learning). The later series turned that into this product:


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

| Structure | Branch |
|---|---|
| frontend / backend / deploy / learning layout, mobile app imported | `branch-21-repository-structure` |
