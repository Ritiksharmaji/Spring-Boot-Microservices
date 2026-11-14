# Notification Service (`notification-service`, port 8084)

Turns `order.status` events into customer emails (confirmed, shipped, delivered, cancelled with reason). Sends via SMTP when `MAIL_HOST` is set, otherwise logs.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/actuator/health` | health |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

Stateless.

## Events

Consumes `order.status` (group `notification-service`, DLQ). See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

RabbitMQ, SMTP (Mailpit locally)

## Configuration

`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from ecommerce-platform/
./mvnw -pl notification-service -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl notification-service test                       # tests: `OrderStatusNotificationTest`
docker compose up -d --build notification-service          # rebuild just this container
```
