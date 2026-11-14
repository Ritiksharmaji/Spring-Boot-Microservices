# Order Service (`order-service`, port 8083)

Shopping cart, checkout and the order lifecycle. Drives the checkout saga: reserves stock, saves the order with a transactional-outbox event, reacts to payment results, publishes status changes.

Key classes: `service/OrderService` (checkout + saga), `service/ProductGateway` / `UserGateway` (resilient clients), `outbox/*`, `messaging/PaymentResultListener`, `model/OrderStatus` (state machine).

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET/DELETE | `/api/cart` | view / clear cart |
| POST | `/api/cart/items` | add item |
| PUT/DELETE | `/api/cart/items/{productId}` | change / remove |
| POST | `/api/orders` | checkout |
| GET | `/api/orders, /api/orders/{id}` | own orders |
| POST | `/api/orders/{id}/cancel` | cancel own order |
| GET | `/api/orders/admin` | all orders (ADMIN) |
| PATCH | `/api/orders/{id}/status` | change status (ADMIN) |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

PostgreSQL `orderdb`: `cart_item`, `orders`, `order_item`, `outbox_event`.

## Events

Publishes `order.created`, `order.status` (via outbox). Consumes `payment.result` (group `order-service`, DLQ). See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

PostgreSQL, RabbitMQ, product-service, user-service

## Configuration

`DB_NAME=orderdb`, `app.outbox.*`, Resilience4j `productService` / `userService` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from backend/microservices/
./mvnw -pl order-service -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl order-service test                       # tests: `OrderFlowIntegrationTest` (15), `OrderPostgresContainerTest`
docker compose up -d --build order-service          # rebuild just this container
```
