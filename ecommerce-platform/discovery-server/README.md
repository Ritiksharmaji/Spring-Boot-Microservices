# Discovery Server (Eureka) (`discovery-server`, port 8761)

Service registry: every service registers its address and heartbeats; clients resolve `lb://<service>` through it.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/` | Eureka dashboard |
| GET | `/eureka/apps` | registry (JSON with `Accept: application/json`) |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

In-memory registry (single node, self-preservation off).

## Events

— See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

—

## Configuration

`EUREKA_HOSTNAME`, `EUREKA_SELF_PRESERVATION` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from ecommerce-platform/
./mvnw -pl discovery-server -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl discovery-server test                       # tests: `EurekaEcommerceApplicationTests` (context)
docker compose up -d --build discovery-server          # rebuild just this container
```
