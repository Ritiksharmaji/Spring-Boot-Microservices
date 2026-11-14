# Application Flow

How every request and event moves through the platform, with sequence diagrams.
For the architecture and the reasons behind it see [SYSTEM-DESIGN.md](SYSTEM-DESIGN.md);
for a hands-on walkthrough with real commands see [STEP-BY-STEP-FLOW.md](STEP-BY-STEP-FLOW.md).

**Flows**
1. [Platform start-up](#1-platform-start-up)
2. [Life of a request through the gateway](#2-life-of-a-request-through-the-gateway)
3. [Login and tokens](#3-login-and-tokens)
4. [Browse the catalog (public)](#4-browse-the-catalog-public)
5. [Admin manages products](#5-admin-manages-products)
6. [Customer profile](#6-customer-profile)
7. [Cart](#7-cart)
8. [Checkout — happy path](#8-checkout--happy-path)
9. [Checkout — payment declined](#9-checkout--payment-declined)
10. [Checkout — out of stock / validation failures](#10-checkout--out-of-stock--validation-failures)
11. [Customer cancels → refund](#11-customer-cancels--refund)
12. [Admin fulfilment: ship and deliver](#12-admin-fulfilment-ship-and-deliver)
13. [Outbox publisher](#13-outbox-publisher)
14. [Configuration refresh (Spring Cloud Bus)](#14-configuration-refresh-spring-cloud-bus)
15. [A downstream service fails](#15-a-downstream-service-fails)

---

## 1. Platform start-up

```mermaid
sequenceDiagram
    autonumber
    participant D as Docker Compose
    participant Infra as Postgres · Mongo · RabbitMQ · Redis · Keycloak
    participant Cfg as config-server
    participant Eu as discovery-server (Eureka)
    participant S as user/product/order/payment/notification
    participant GW as api-gateway

    D->>Infra: start, wait for health checks
    D->>Cfg: start (needs RabbitMQ healthy)
    D->>Eu: start
    Note over Cfg,Eu: both report readiness UP
    D->>S: start (needs config + Eureka + their DB healthy)
    S->>Cfg: GET /{service}/default  (application.yml + {service}.yml)
    S->>Infra: connect DB, declare RabbitMQ exchanges/queues (+ DLQs)
    S->>Eu: register (IP:port), heartbeat every 30 s
    D->>GW: start (needs config + Eureka + Redis)
    GW->>Cfg: fetch api-gateway.yml (routes policy, CORS, Keycloak URLs)
    GW->>Eu: fetch registry (lb://service-name → instances)
```

---

## 2. Life of a request through the gateway

Every `/api/**` call follows this path.

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant N as Nginx (prod)
    participant GW as API Gateway
    participant KC as Keycloak (JWKS)
    participant R as Redis
    participant Eu as Eureka cache
    participant S as Target service

    C->>N: HTTPS GET /api/orders  Authorization: Bearer <JWT>
    N->>GW: proxy, X-Forwarded-For = client IP (overwritten)
    GW->>GW: CORS (pre-flight answered here)
    GW->>KC: JWKS (first time only, then cached)
    GW->>GW: verify signature · iss · exp → roles from realm_access / client roles
    GW->>GW: authorize path+method (public / CUSTOMER / ADMIN / deny)
    alt not authenticated / not allowed
        GW-->>C: 401 / 403
    end
    GW->>GW: UserContextFilter: drop client X-User-*; set X-User-ID=sub, X-User-Email, X-User-Roles
    GW->>R: rate-limit token bucket (catalog route)
    GW->>Eu: resolve lb://order-service → instance
    GW->>S: forward (circuit breaker; traceparent header)
    S-->>GW: response (RFC 7807 problem+json on errors)
    GW-->>C: response
```

---

## 3. Login and tokens

**Browser / mobile app (production):** Authorization Code + PKCE.

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant App as Frontend (React)
    participant KC as Keycloak
    participant GW as API Gateway

    App->>App: create code_verifier + code_challenge (S256)
    App->>KC: /auth?client_id=oauth2-ecom-pkce&code_challenge=...
    U->>KC: login (or self-register → role CUSTOMER by default)
    KC-->>App: redirect with ?code=...
    App->>KC: POST /token (code + code_verifier)
    KC-->>App: access_token (15 min) + refresh_token
    App->>GW: API calls with Bearer access_token
    App->>KC: refresh_token grant before expiry
```

**Local testing / Postman:** password grant (enabled only in the local realm).

```bash
curl -s -X POST http://localhost:8483/realms/ecom-app/protocol/openid-connect/token \
  -d grant_type=password -d client_id=oauth2-ecom-pkce \
  -d username=customer -d password=Customer@123 | jq -r .access_token
```

The token's `iss` is always `http://localhost:8483/realms/ecom-app` (local) or
`https://AUTH_DOMAIN/realms/ecom-app` (production), because Keycloak runs with a fixed
`KC_HOSTNAME`, while the gateway downloads signing keys over the internal network.

---

## 4. Browse the catalog (public)

```mermaid
sequenceDiagram
    participant C as Anyone
    participant GW as Gateway
    participant P as product-service
    participant DB as productdb

    C->>GW: GET /api/products?category=electronics&page=0&size=20
    GW->>GW: GET /api/products/** is permitAll · rate limit · retry (GET) · breaker
    GW->>P: forward
    P->>DB: SELECT ... WHERE active AND category ILIKE ... LIMIT 20
    P-->>C: { content: [...], page: { size, number, totalElements, totalPages } }
```

Also `GET /api/products/{id}` (404 if missing or soft-deleted) and
`GET /api/products/search?keyword=...` (name/description, in stock only).

---

## 5. Admin manages products

```mermaid
sequenceDiagram
    participant A as Admin
    participant GW as Gateway
    participant P as product-service

    A->>GW: POST /api/products {name, price, stockQuantity, ...}
    GW->>GW: write on /api/products/** requires ROLE_ADMIN (403 otherwise)
    GW->>P: forward
    P->>P: Bean Validation (400 with field errors)
    P-->>A: 201 product
    A->>GW: PUT /api/products/{id}  → 200
    A->>GW: DELETE /api/products/{id} → 204 (active=false, kept for order history)
```

---

## 6. Customer profile

The Keycloak account (login) and the shop profile (address, phone) are separate; the profile is
linked to the account by the JWT `sub`.

```mermaid
sequenceDiagram
    participant C as Customer
    participant GW as Gateway
    participant U as user-service
    participant M as MongoDB

    C->>GW: PUT /api/users/me {firstName, email, phone, address}
    GW->>U: X-User-ID = sub (set by gateway)
    U->>M: find by keycloakId → create or update
    U->>U: role is never taken from the request (stays CUSTOMER)
    U->>M: unique email index (409 if used by another account)
    U-->>C: 200 profile
```

---

## 7. Cart

```mermaid
sequenceDiagram
    participant C as Customer
    participant O as order-service
    participant P as product-service

    C->>O: POST /api/cart/items {productId, quantity}
    O->>P: GET /api/products/{id}  (retry + breaker)
    alt product missing / inactive
        O-->>C: 404
    else quantity in cart + new > stock
        O-->>C: 409 Insufficient Stock
    else ok
        O->>O: upsert cart line (merge quantity), store live price + name
        O-->>C: 201 {items, totalItems, totalAmount}
    end
    C->>O: PUT /api/cart/items/{productId} {quantity}  (re-checks stock)
    C->>O: DELETE /api/cart/items/{productId} · DELETE /api/cart
```

The cart shows prices at the time of adding; checkout always re-reads current prices.

---

## 8. Checkout — happy path

```mermaid
sequenceDiagram
    autonumber
    participant C as Customer
    participant O as order-service
    participant U as user-service
    participant P as product-service
    participant DB as orderdb
    participant MQ as RabbitMQ
    participant Pay as payment-service
    participant N as notification-service
    participant M as Mail

    C->>O: POST /api/orders
    O->>DB: load cart (422 Empty Cart if none)
    O->>U: GET /internal/users/{sub} (422 Profile Incomplete if no address)
    O->>P: GET each product → current price, still active?
    O->>P: POST /internal/products/stock/reserve {items}
    P->>P: one TX: UPDATE ... WHERE stock >= qty for each line
    P-->>O: 204
    O->>DB: TX: INSERT order PENDING + items, DELETE cart, INSERT outbox OrderCreated
    O-->>C: 201 {id, status: PENDING, statusReason: "Awaiting payment", totalAmount, items, shippingAddress}
    O->>MQ: (outbox publisher, ≤1 s) order.created
    MQ->>Pay: queue order.created.payment-service
    Pay->>Pay: TX: no payment for orderId yet → charge → INSERT COMPLETED (TXN-...)
    Pay->>MQ: payment.result COMPLETED
    MQ->>O: queue payment.result.order-service
    O->>DB: TX: PENDING → CONFIRMED, outbox OrderStatusChanged
    O->>MQ: order.status CONFIRMED
    MQ->>N: queue order.status.notification-service
    N->>M: "Order #42 confirmed"
    MQ->>Pay: queue order.status.payment-service (ignored: not CANCELLED)
    C->>O: GET /api/orders/42 → CONFIRMED
```

---

## 9. Checkout — payment declined

The simulated provider declines amounts above `PAYMENT_MAX_AMOUNT` (default 50 000.00).

```mermaid
sequenceDiagram
    autonumber
    participant O as order-service
    participant P as product-service
    participant MQ as RabbitMQ
    participant Pay as payment-service
    participant N as notification-service

    Note over O: order 43 PENDING, stock already reserved
    MQ->>Pay: order.created (total 60 000.00)
    Pay->>Pay: provider declines → INSERT FAILED (reason)
    Pay->>MQ: payment.result FAILED "Amount ... exceeds the card limit"
    MQ->>O: payment.result
    O->>O: TX: PENDING → CANCELLED (statusReason "Payment failed: ..."), outbox
    O->>P: compensation: POST /internal/products/stock/release
    O->>MQ: order.status CANCELLED
    MQ->>N: email "Order #43 cancelled: Payment failed: ..."
    MQ->>Pay: order.status CANCELLED → payment is FAILED, nothing to refund
```

---

## 10. Checkout — out of stock / validation failures

| Situation | Response | Side effects |
|---|---|---|
| Cart empty | `422 Empty Cart` | none |
| No profile / no address | `422 Profile Incomplete` | none (stock untouched) |
| Product deleted since it was added | `422 Product Unavailable` | none |
| Stock taken by someone else meanwhile | `409 Insufficient Stock` | reservation rolled back in product-service; cart kept so the customer can adjust |
| product-service unreachable | `503 Service Unavailable` after 3 attempts (or immediately while the breaker is open) | none |
| DB failure after stock reserved | `500` | reservation **released** (compensation) |

---

## 11. Customer cancels → refund

```mermaid
sequenceDiagram
    autonumber
    participant C as Customer
    participant O as order-service
    participant P as product-service
    participant MQ as RabbitMQ
    participant Pay as payment-service
    participant N as notification-service

    C->>O: POST /api/orders/42/cancel
    O->>O: own order? (404 otherwise) · status PENDING/CONFIRMED? (422 otherwise)
    O->>O: TX: → CANCELLED "Cancelled by customer", outbox
    O->>P: release stock
    O-->>C: 200 {status: CANCELLED}
    O->>MQ: order.status CANCELLED
    MQ->>Pay: COMPLETED payment → REFUNDED (provider.refund)
    Note over Pay: if no payment exists yet → record SKIPPED,<br/>a late order.created is then ignored (never charged)
    MQ->>N: email "Order #42 cancelled ... will be refunded"
```

---

## 12. Admin fulfilment: ship and deliver

```mermaid
sequenceDiagram
    participant A as Admin
    participant GW as Gateway
    participant O as order-service
    participant MQ as RabbitMQ
    participant N as notification-service

    A->>GW: GET /api/orders/admin  (ROLE_ADMIN at gateway + X-User-Roles check in service)
    A->>GW: PATCH /api/orders/42/status {"status":"SHIPPED"}
    GW->>O: forward
    O->>O: CONFIRMED → SHIPPED allowed? (422 for e.g. PENDING → SHIPPED)
    O->>MQ: order.status SHIPPED → email "has shipped"
    A->>GW: PATCH /api/orders/42/status {"status":"DELIVERED"} → email "delivered"
```

---

## 13. Outbox publisher

```mermaid
flowchart LR
    tx[Business TX<br/>order change + outbox row] --> table[(outbox_event<br/>published_at = null)]
    sched[@Scheduled every 1 s] --> read[read 50 oldest unpublished]
    read --> send{StreamBridge.send<br/>accepted?}
    send -- yes --> mark[published_at = now]
    send -- no / error --> keep[attempts++, last_error<br/>stop batch, retry next tick]
    mark --> purge[nightly purge > 7 days]
```

Ordering is preserved (oldest first, stop at the first failure). Delivery is at-least-once, which
is why every consumer is idempotent.

---

## 14. Configuration refresh (Spring Cloud Bus)

```mermaid
sequenceDiagram
    participant Ops as Operator
    participant Cfg as config-server
    participant MQ as RabbitMQ (springCloudBus)
    participant S as all services

    Ops->>Cfg: edit config file (CONFIG_REPO_LOCATION) 
    Ops->>Cfg: POST /actuator/busrefresh
    Cfg->>MQ: RefreshRemoteApplicationEvent
    MQ->>S: every instance re-reads its configuration
    Note over S: @RefreshScope beans (e.g. app.message) pick up new values without restart
```

---

## 15. A downstream service fails

```mermaid
sequenceDiagram
    participant C as Customer
    participant O as order-service
    participant CB as Resilience4j (productService)
    participant P as product-service (down)

    C->>O: POST /api/cart/items
    O->>CB: getProduct(1)
    CB->>P: attempt 1 ✗ (connection refused)
    CB->>P: attempt 2 after 300 ms ✗
    CB->>P: attempt 3 after 600 ms ✗
    CB-->>O: fallback → ServiceUnavailableException
    O-->>C: 503 {"title":"Service Unavailable","detail":"Product service is unavailable, please try again later"}
    Note over CB: after ≥5 calls with ≥50 % failures the breaker OPENS:<br/>calls fail immediately for 10 s, then HALF_OPEN probes recovery
```
