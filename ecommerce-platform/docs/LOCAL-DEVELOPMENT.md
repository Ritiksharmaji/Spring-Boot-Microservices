# Local Development

## Option A — everything in Docker

```bash
cd ecommerce-platform
docker compose up -d --build
docker compose logs -f order-service           # follow a service
docker compose up -d --build order-service     # rebuild one service after a change
```

## Option B — infrastructure in Docker, services in the IDE (debugging)

```bash
docker compose up -d postgres mongodb rabbitmq redis keycloak zipkin mailpit
```

Open `ecommerce-platform/pom.xml` in IntelliJ IDEA (Maven project, **JDK 21**, Lombok plugin +
annotation processing enabled). Run in this order — defaults already point to `localhost`:

1. `config-server` (`SpringCloudServerEcommerceApplication`)
2. `discovery-server` (`EurekaEcommerceApplication`)
3. `user-service`, `product-service`, `order-service`, `payment-service`, `notification-service` (`EcomApplication` / `PaymentServiceApplication` / `NotificationApplication`)
4. `api-gateway` (`ApiGateWayApplication`)

For email locally set `MAIL_HOST=localhost` on notification-service (Mailpit), otherwise emails are logged.

Mixed mode also works: run the whole stack in Docker, stop one service
(`docker compose stop order-service`) and run that one from the IDE — the other containers find it
through Eureka only if it registers with a reachable address, so prefer Option B for debugging.

## Build & test

```bash
./mvnw clean verify            # build + all tests (+ Testcontainers if Docker is running)
./mvnw -pl order-service -am spring-boot:run     # run one service from the command line
```

## Conventions

- Branches: `branch-18-<n>-<description>` for this completion series (see [PROJECT-JOURNEY.md](PROJECT-JOURNEY.md)).
- Errors: throw a domain exception; the `GlobalExceptionHandler` maps it to RFC 7807.
- New config: add to `config-server/src/main/resources/config/<service>.yml` with `${ENV:default}`,
  document it in [CONFIGURATION.md](CONFIGURATION.md) and `.env.example`.
- New event: record in `messaging/`, binding in the service yml, consumer must be idempotent,
  document in [EVENTS.md](EVENTS.md).
- Every new endpoint: validation, ownership check, gateway rule in `SecurityConfig`, test.

## Handy commands

```bash
docker compose exec postgres psql -U ecom -d orderdb                         # SQL shell
docker compose exec mongodb mongosh userdb                                    # Mongo shell
docker compose exec rabbitmq rabbitmqctl list_queues name messages consumers  # queues
curl -s localhost:8761/eureka/apps -H 'Accept: application/json' | jq '.applications.application[].name'
curl -s localhost:8080/actuator/health | jq
```
