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
| `order-service` | 8083 | Cart and orders (PostgreSQL); calls user and product services; drives the order saga (transactional outbox) |
| `notification-service` | 8084 | Emails customers on order status changes (consumes `order.status`) |
| `payment-service` | 8085 | Charges / refunds orders (consumes `order.created`, publishes `payment.result`) |

Service configuration lives in `config-server/src/main/resources/config/`:
`application.yml` holds shared settings, `{service-name}.yml` holds per-service settings.
All hosts and credentials come from environment variables — see [`.env.example`](.env.example).

## Security model

The API gateway validates the Keycloak JWT and is the only public entry point.

| Role | Can do |
|---|---|
| anonymous | browse and search products |
| `CUSTOMER` | own profile (`/api/users/me`), cart, place / view / cancel own orders |
| `ADMIN` | everything above + manage products, list users, all orders, change order status |

The gateway **removes any `X-User-*` header sent by the client** and sets
`X-User-ID` (JWT `sub`), `X-User-Email` and `X-User-Roles` from the verified token,
so a caller can never act as another user. `/internal/**` endpoints (service-to-service)
are never routed by the gateway.

## API overview (through the gateway, port 8080)

| Method | Path | Who | Description |
|---|---|---|---|
| GET | `/api/products?category=&page=&size=` | public | Paged catalog |
| GET | `/api/products/{id}`, `/api/products/search?keyword=` | public | Product details / search |
| POST, PUT, DELETE | `/api/products[/{id}]` | ADMIN | Manage catalog (delete = soft delete) |
| GET, PUT | `/api/users/me` | CUSTOMER | View / create-or-update own profile and address |
| GET, POST, PUT | `/api/users[/{id}]` | ADMIN | Manage users |
| GET, DELETE | `/api/cart` | CUSTOMER | View / clear cart (live prices, totals) |
| POST | `/api/cart/items` | CUSTOMER | Add product (stock-checked) |
| PUT, DELETE | `/api/cart/items/{productId}` | CUSTOMER | Change quantity / remove |
| POST | `/api/orders` | CUSTOMER | Checkout |
| GET | `/api/orders`, `/api/orders/{id}` | CUSTOMER | Own order history / details |
| POST | `/api/orders/{id}/cancel` | CUSTOMER | Cancel (PENDING/CONFIRMED only, stock is returned) |
| GET | `/api/orders/admin` | ADMIN | All orders |
| GET | `/api/payments/orders/{orderId}` | CUSTOMER | Payment status of own order |
| GET | `/api/payments/admin` | ADMIN | All payments |
| PATCH | `/api/orders/{id}/status` | ADMIN | `CONFIRMED → SHIPPED → DELIVERED` |

Errors are returned as RFC 7807 `application/problem+json`
(400 validation, 404 not found, 409 insufficient stock, 422 invalid state, 503 dependency down).

### Checkout flow

1. Cart must not be empty; the customer profile must have a delivery address.
2. Current prices are re-read from product-service (cart prices may be stale).
3. Stock for all lines is reserved in **one atomic call** (`UPDATE … WHERE stock >= qty`),
   all-or-nothing, so concurrent orders cannot oversell.
4. The order is saved as **PENDING** with a snapshot of prices, customer email and shipping address;
   the cart is cleared and an `OrderCreated` event is written to the **outbox table in the same transaction**.
   If saving fails, the reservation is released (compensation).
5. The outbox publisher sends `order.created` to RabbitMQ; payment-service charges the order (idempotent)
   and publishes `payment.result`.
6. order-service moves the order to **CONFIRMED**, or to **CANCELLED** and releases the stock if the payment
   failed, and publishes `order.status`; notification-service emails the customer, payment-service refunds
   cancelled orders.

Calls to product/user services go through Resilience4j: retries with exponential back-off for
reads, a circuit breaker per service, and business errors (404/409) never count as failures.
Stock reservation is not retried because it is not idempotent.

## Build

```bash
./mvnw clean package -DskipTests
```

Requires JDK 21. All modules share one parent POM (Spring Boot 3.5.9, Spring Cloud 2025.0.1).

## Run locally

Start order: `config-server` → `discovery-server` → `user-service`, `product-service`, `order-service`,
`notification-service`, `payment-service` → `api-gateway`.

Infrastructure needed on localhost (a full `docker-compose` is added in `branch-18-4`):
PostgreSQL, MongoDB, RabbitMQ, Redis, Keycloak, Zipkin, Mailpit.
