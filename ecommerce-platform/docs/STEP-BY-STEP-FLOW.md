# Step-by-Step Flow — run it and watch it work

A guided tour: start the platform, then perform a complete shopping journey with `curl`
(or the Postman collection) and, at every step, see **what happened inside** the system and
**where you can observe it**.

> Prefer clicking? Import `postman/E-Commerce-Platform.postman_collection.json` and
> `postman/Local.postman_environment.json` and run the folders in order — they follow
> exactly these steps. See [POSTMAN.md](POSTMAN.md).

---

## Step 0 — Prerequisites

| Tool | Version | Check |
|---|---|---|
| Docker Desktop / Docker Engine + Compose v2 | 24+ | `docker compose version` |
| RAM available to Docker | ≥ 6 GB (8 GB with observability) | Docker Desktop → Settings → Resources |
| curl, jq | any | `jq --version` |
| JDK 21 (only to build/run outside Docker) | 21 | `java -version` |

## Step 1 — Start the platform

```bash
git clone https://github.com/Ritiksharmaji/Spring-Boot-Microservices.git
cd Spring-Boot-Microservices/ecommerce-platform
docker compose up -d --build          # first build ~3-5 min
docker compose ps                     # wait until every service shows (healthy)
```

**What happens:** infrastructure starts first (Postgres creates `productdb`, `orderdb`,
`paymentdb`, `keycloakdb`; Keycloak imports realm `ecom-app` with roles and test users), then
config-server and Eureka, then the services (each fetches its config and registers in Eureka),
finally the gateway.

**Observe:** Eureka dashboard http://localhost:8761 lists `API-GATEWAY`, `USER-SERVICE`,
`PRODUCT-SERVICE`, `ORDER-SERVICE`, `PAYMENT-SERVICE`, `NOTIFICATION-SERVICE`.
RabbitMQ http://localhost:15672 (guest/guest) → *Exchanges* shows `order.created`,
`order.status`, `payment.result`, `springCloudBus`.

Optional dashboards: `docker compose --profile observability up -d` → Grafana http://localhost:3001.

## Step 2 — Get tokens

```bash
KC=http://localhost:8483/realms/ecom-app/protocol/openid-connect/token
ADMIN=$(curl -s -X POST $KC -d grant_type=password -d client_id=oauth2-ecom-pkce \
        -d username=admin -d password='Admin@123' | jq -r .access_token)
CUSTOMER=$(curl -s -X POST $KC -d grant_type=password -d client_id=oauth2-ecom-pkce \
        -d username=customer -d password='Customer@123' | jq -r .access_token)
GW=http://localhost:8080
```

**Inside:** Keycloak issues RS256-signed JWTs. Paste one into https://jwt.io to see
`sub`, `email`, `realm_access.roles` (`CUSTOMER`, and `ADMIN` for the admin).

| User | Password | Roles |
|---|---|---|
| `admin` | `Admin@123` | ADMIN, CUSTOMER |
| `customer` | `Customer@123` | CUSTOMER |
| `customer2` | `Customer@123` | CUSTOMER |

## Step 3 — Security in action

```bash
curl -i $GW/api/cart                                             # 401: no token
curl -i -X POST $GW/api/products -H "Authorization: Bearer $CUSTOMER" \
     -H 'Content-Type: application/json' -d '{}'                 # 403: customers cannot manage products
curl -i $GW/api/cart -H "Authorization: Bearer $CUSTOMER" \
     -H 'X-User-ID: someone-else'                                # 200: your own cart — spoofed header ignored
```

**Inside:** the gateway validated the token, checked the role rule for the path and replaced
`X-User-ID` with the token's `sub`.

## Step 4 — Admin creates products

```bash
curl -s -X POST $GW/api/products -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"name":"Mechanical Keyboard","description":"RGB, blue switches","price":499.50,"stockQuantity":5,"category":"electronics"}' | jq
curl -s -X POST $GW/api/products -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"name":"Gaming Laptop","price":60000.00,"stockQuantity":1,"category":"electronics"}' | jq
PID=1; LAPTOP=2      # use the ids returned above
```

**Try a validation error:** `-d '{"name":"","price":-1}'` → `400` with `errors.name`, `errors.price`, `errors.stockQuantity`.

## Step 5 — Anyone browses

```bash
curl -s "$GW/api/products?category=electronics&page=0&size=10" | jq '.content[] | {id,name,price,stockQuantity}'
curl -s "$GW/api/products/search?keyword=keyboard" | jq '.page'
```

**Observe:** Zipkin http://localhost:9411 → *Run query* → a trace `api-gateway → product-service`.

## Step 6 — Customer creates a profile

```bash
curl -s -X PUT $GW/api/users/me -H "Authorization: Bearer $CUSTOMER" -H 'Content-Type: application/json' -d '{
  "firstName":"Riya","lastName":"Customer","email":"customer@shop.local","phone":"+919876543210",
  "address":{"street":"MG Road 1","city":"Pune","state":"MH","country":"India","zipcode":"411001"}}' | jq
```

**Inside:** user-service stores the profile in MongoDB keyed by the token's `sub`; the role
cannot be changed through this endpoint.

## Step 7 — Fill the cart

```bash
curl -s -X POST $GW/api/cart/items -H "Authorization: Bearer $CUSTOMER" -H 'Content-Type: application/json' \
  -d "{\"productId\":$PID,\"quantity\":2}" | jq        # totalAmount 999.00
curl -s -X POST $GW/api/cart/items -H "Authorization: Bearer $CUSTOMER" -H 'Content-Type: application/json' \
  -d "{\"productId\":$PID,\"quantity\":10}" | jq       # 409 Insufficient Stock
```

**Inside:** order-service called product-service through Eureka (`lb://product-service`) with
retry + circuit breaker, then stored the line with the live price.

## Step 8 — Checkout

```bash
ORDER=$(curl -s -X POST $GW/api/orders -H "Authorization: Bearer $CUSTOMER"); echo $ORDER | jq
OID=$(echo $ORDER | jq -r .id)                          # status: PENDING, "Awaiting payment"
sleep 2
curl -s $GW/api/orders/$OID -H "Authorization: Bearer $CUSTOMER" | jq '{id,status,statusReason,totalAmount}'
#   → CONFIRMED, "Payment received"
curl -s $GW/api/payments/orders/$OID -H "Authorization: Bearer $CUSTOMER" | jq   # COMPLETED, TXN-...
curl -s $GW/api/products/$PID | jq .stockQuantity                                 # 3
```

**Inside (≈1 s):**
1. product-service reserved 2 units atomically.
2. order-service saved the order **PENDING** + an `OrderCreated` row in `outbox_event` in one transaction.
3. The outbox publisher sent it to exchange `order.created`.
4. payment-service charged it and published `payment.result COMPLETED`.
5. order-service moved the order to **CONFIRMED** and published `order.status`.
6. notification-service sent an email.

**Observe:**
- **Mailpit** http://localhost:8025 → email "Order #N confirmed".
- **Zipkin** → one trace spanning gateway → order → product/user, and the asynchronous hops.
- **RabbitMQ** → *Queues* → message rates on `order.created.payment-service`, `payment.result.order-service`, `order.status.notification-service`.
- **Postgres:** `docker compose exec postgres psql -U ecom -d orderdb -c "select id,event_type,published_at from outbox_event order by id;"`

## Step 9 — Payment declined (saga compensation)

```bash
curl -s -X POST $GW/api/cart/items -H "Authorization: Bearer $CUSTOMER" -H 'Content-Type: application/json' \
  -d "{\"productId\":$LAPTOP,\"quantity\":1}" > /dev/null
OID2=$(curl -s -X POST $GW/api/orders -H "Authorization: Bearer $CUSTOMER" | jq -r .id); sleep 2
curl -s $GW/api/orders/$OID2 -H "Authorization: Bearer $CUSTOMER" | jq '{status,statusReason}'
#   → CANCELLED, "Payment failed: Amount 60000.00 exceeds the card limit of 50000.00"
curl -s $GW/api/products/$LAPTOP | jq .stockQuantity    # 1 again: stock released
```

**Observe:** Mailpit → "Order #N cancelled: Payment failed: ...".

## Step 10 — Customer cancels → refund

```bash
curl -s -X POST $GW/api/cart/items -H "Authorization: Bearer $CUSTOMER" -H 'Content-Type: application/json' \
  -d "{\"productId\":$PID,\"quantity\":1}" > /dev/null
OID3=$(curl -s -X POST $GW/api/orders -H "Authorization: Bearer $CUSTOMER" | jq -r .id); sleep 2
curl -s -X POST $GW/api/orders/$OID3/cancel -H "Authorization: Bearer $CUSTOMER" | jq .status   # CANCELLED
sleep 1; curl -s $GW/api/payments/orders/$OID3 -H "Authorization: Bearer $CUSTOMER" | jq .status  # REFUNDED
```

## Step 11 — Admin ships and delivers

```bash
curl -s $GW/api/orders/admin -H "Authorization: Bearer $ADMIN" | jq '.content[] | {id,userId,status}'
curl -s -X PATCH $GW/api/orders/$OID/status -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' -d '{"status":"SHIPPED"}' | jq .status
curl -s -X PATCH $GW/api/orders/$OID/status -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' -d '{"status":"DELIVERED"}' | jq .status
curl -s -X POST $GW/api/orders/$OID/cancel -H "Authorization: Bearer $CUSTOMER" | jq .title    # "Invalid Status Change" (422)
```

**Observe:** Mailpit → "has shipped", "delivered".

## Step 12 — Isolation between customers

```bash
OTHER=$(curl -s -X POST $KC -d grant_type=password -d client_id=oauth2-ecom-pkce -d username=customer2 -d password='Customer@123' | jq -r .access_token)
curl -i $GW/api/orders/$OID -H "Authorization: Bearer $OTHER"     # 404 (not 403: order ids cannot be probed)
```

## Step 13 — Resilience demo

```bash
docker compose stop product-service
curl -s -X POST $GW/api/cart/items -H "Authorization: Bearer $CUSTOMER" -H 'Content-Type: application/json' \
  -d "{\"productId\":$PID,\"quantity\":1}" | jq        # 503 after 3 retries
curl -s $GW/api/products | jq                          # gateway fallback: "Product service is unavailable"
docker compose start product-service                   # recovers automatically (breaker half-open → closed)
```

Broker outage: `docker compose stop rabbitmq`, place an order (still `201 PENDING`, event waits
in `outbox_event`), `docker compose start rabbitmq` → the order becomes CONFIRMED by itself.

## Step 14 — API docs

Swagger UI for every service: http://localhost:8080/swagger-ui.html (pick the service at the top
right, *Authorize* with a token from Step 2).

## Step 15 — Everything at once

```bash
./scripts/smoke-test.sh      # the same journey, automated, ~60 assertions (also runs in CI)
```

## Step 16 — Stop

```bash
docker compose down          # keep data
docker compose down -v       # wipe databases too
```
