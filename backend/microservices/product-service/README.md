# Product Service (`product-service`, port 8082)

Product catalog (paged listing, category filter, search, soft delete) and the **atomic stock reservation** used at checkout.

Reservation: `UPDATE Product SET stockQuantity = stockQuantity - :q WHERE id = :id AND active AND stockQuantity >= :q` per line inside one transaction — 0 rows updated → 409 and full rollback.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/api/products, /api/products/{id}, /api/products/search` | public catalog |
| POST/PUT/DELETE | `/api/products[/{id}]` | manage (ADMIN) |
| POST | `/internal/products/stock/reserve` | all-or-nothing reservation (order-service) |
| POST | `/internal/products/stock/release` | return stock (order-service) |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

PostgreSQL `productdb`, table `products`.

## Events

Spring Cloud Bus only. See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

PostgreSQL

## Configuration

`DB_HOST`, `DB_NAME=productdb`, `DB_USERNAME`, `DB_PASSWORD` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from backend/microservices/
./mvnw -pl product-service -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl product-service test                       # tests: `ProductApiIntegrationTest`, `ProductStockConcurrencyContainerTest`
docker compose up -d --build product-service          # rebuild just this container
```
