# Payment Service (`payment-service`, port 8085)

Charges orders from `order.created` exactly once (unique `orderId`), publishes the outcome, refunds cancelled orders and never charges orders cancelled before payment. Uses a simulated provider behind the `PaymentProvider` interface (declines above `PAYMENT_MAX_AMOUNT`).

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/api/payments/orders/{orderId}` | payment of own order |
| GET | `/api/payments/admin` | all payments (ADMIN) |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

PostgreSQL `paymentdb`, table `payments` (statuses COMPLETED, FAILED, REFUNDED, SKIPPED).

## Events

Consumes `order.created`, `order.status` (groups `payment-service`, DLQ). Publishes `payment.result`. See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

PostgreSQL, RabbitMQ

## Configuration

`DB_NAME=paymentdb`, `PAYMENT_MAX_AMOUNT` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from backend/microservices/
./mvnw -pl payment-service -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl payment-service test                       # tests: `PaymentSagaTest` (6), `PaymentRabbitMqContainerTest`
docker compose up -d --build payment-service          # rebuild just this container
```
