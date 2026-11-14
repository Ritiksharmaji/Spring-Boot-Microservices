# Config Server (`config-server`, port 8888)

Serves configuration to every service (Spring Cloud Config, `native` profile) and broadcasts refresh events over Spring Cloud Bus (RabbitMQ).

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/{application}/{profile}` | configuration for a service, e.g. `/order-service/default` |
| POST | `/actuator/busrefresh` | tell all services to reload `@RefreshScope` beans |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

Files in `src/main/resources/config/` (shared `application.yml` + `<service>.yml`). Override the location with `CONFIG_REPO_LOCATION`.

## Events

Publishes `RefreshRemoteApplicationEvent` on the `springCloudBus` exchange. See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

RabbitMQ

## Configuration

`CONFIG_REPO_LOCATION`, `RABBITMQ_*` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from backend/microservices/
./mvnw -pl config-server -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl config-server test                       # tests: `SpringCloudServerEcommerceApplicationTests` (context)
docker compose up -d --build config-server          # rebuild just this container
```
