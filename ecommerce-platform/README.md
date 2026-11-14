# E-Commerce Platform — Spring Boot Microservices

Production version of the e-commerce backend built through the learning branches
(`branch-2` … `branch-17-2`). Completion work happens on the `branch-18-*` series.

## Services

| Service | Port | Responsibility |
|---|---|---|
| `config-server` | 8888 | Centralized configuration (Spring Cloud Config, native profile) |
| `discovery-server` | 8761 | Service registry (Netflix Eureka) |
| `api-gateway` | 8080 | Single entry point: routing, Keycloak JWT security, Redis rate limiting, circuit breakers |
| `user-service` | 8081 | Users and addresses (MongoDB) |
| `product-service` | 8082 | Product catalog (PostgreSQL) |
| `order-service` | 8083 | Cart and orders (PostgreSQL); calls user and product services; publishes order events |
| `notification-service` | 8084 | Consumes order events |

Service configuration lives in `config-server/src/main/resources/config/`:
`application.yml` holds shared settings, `{service-name}.yml` holds per-service settings.
All hosts and credentials come from environment variables — see [`.env.example`](.env.example).

## Build

```bash
./mvnw clean package -DskipTests
```

Requires JDK 21. All modules share one parent POM (Spring Boot 3.5.9, Spring Cloud 2025.0.1).

## Run locally

Start order: `config-server` → `discovery-server` → `user-service`, `product-service`, `order-service`,
`notification-service` → `api-gateway`.

Infrastructure needed on localhost (a full `docker-compose` is added in `branch-18-4`):
PostgreSQL, MongoDB, RabbitMQ, Kafka, Redis, Keycloak, Zipkin.
