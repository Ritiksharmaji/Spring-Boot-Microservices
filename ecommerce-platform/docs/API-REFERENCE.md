# API Reference

Base URL: `http://localhost:8080` (local) or `https://API_DOMAIN` (production) — always through
the API gateway. Interactive docs: **`/swagger-ui.html`** (all services, switch at the top right).

Authentication: `Authorization: Bearer <access_token>` from Keycloak realm `ecom-app`
(see [STEP-BY-STEP-FLOW.md](STEP-BY-STEP-FLOW.md#step-2--get-tokens)).

## Conventions

- JSON in/out; money as decimal numbers with 2 decimals; timestamps ISO-8601 (`Asia/Kolkata`).
- **Paged** responses:
  ```json
  { "content": [ ... ], "page": { "size": 20, "number": 0, "totalElements": 42, "totalPages": 3 } }
  ```
  Query parameters: `page` (0-based), `size`, `sort=field,asc|desc`.
- **Errors** are RFC 7807 `application/problem+json`:
  ```json
  { "type": "about:blank", "title": "Insufficient Stock", "status": 409,
    "detail": "Only 3 unit(s) of 'Mechanical Keyboard' available", "instance": "/api/cart/items" }
  ```
  Validation errors add `"errors": { "field": "message" }`.

| Status | Meaning |
|---|---|
| 400 | validation failed (`errors` map) |
| 401 | missing / invalid / expired token |
| 403 | authenticated but role not allowed |
| 404 | not found — also returned for resources owned by someone else |
| 409 | conflict: insufficient stock, email already registered |
| 422 | not possible in the current state: empty cart, incomplete profile, invalid status change |
| 429 | rate limit exceeded |
| 503 | a downstream service is unavailable (retries exhausted / circuit open) |

---

## Products — `product-service`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/products?category=&page=&size=&sort=` | public | active products, paged, optional category (case-insensitive) |
| GET | `/api/products/{id}` | public | one active product (404 if missing/deleted) |
| GET | `/api/products/search?keyword=` | public | in-stock products whose name or description contains the keyword |
| POST | `/api/products` | ADMIN | create → 201 |
| PUT | `/api/products/{id}` | ADMIN | replace fields → 200 |
| DELETE | `/api/products/{id}` | ADMIN | soft delete → 204 |

**ProductRequest**
```json
{ "name": "Mechanical Keyboard", "description": "RGB", "price": 499.50,
  "stockQuantity": 5, "category": "electronics", "imageUrl": "https://...", "active": true }
```
Rules: `name` required ≤150; `description` ≤255; `price` > 0, 2 decimals; `stockQuantity` ≥ 0.

**ProductResponse**
```json
{ "id": 1, "name": "Mechanical Keyboard", "description": "RGB", "price": 499.50, "stockQuantity": 5,
  "category": "electronics", "imageUrl": null, "active": true,
  "createdAt": "2026-11-15T10:00:00", "updatedAt": "2026-11-15T10:00:00" }
```

## Users — `user-service`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/users/me` | CUSTOMER | own profile (404 until created) |
| PUT | `/api/users/me` | CUSTOMER | create or update own profile (role ignored) |
| GET | `/api/users` | ADMIN | all users |
| GET | `/api/users/{id}` | ADMIN | one user |
| POST | `/api/users` | ADMIN | create user (role allowed) → 201 |
| PUT | `/api/users/{id}` | ADMIN | update user |

**UserRequest**
```json
{ "firstName": "Riya", "lastName": "Customer", "email": "customer@shop.local", "phone": "+919876543210",
  "role": "CUSTOMER",
  "address": { "street": "MG Road 1", "city": "Pune", "state": "MH", "country": "India", "zipcode": "411001" } }
```
Rules: `firstName` required; valid unique `email` (409 if taken); `phone` 7–15 digits with optional `+`;
address `street`, `city`, `country`, `zipcode` required when an address is given.

## Cart — `order-service`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/cart` | CUSTOMER | current cart |
| POST | `/api/cart/items` | CUSTOMER | add `{productId, quantity 1-100}` (merges) → 201 cart |
| PUT | `/api/cart/items/{productId}` | CUSTOMER | set quantity `{quantity}` → cart |
| DELETE | `/api/cart/items/{productId}` | CUSTOMER | remove line → 204 |
| DELETE | `/api/cart` | CUSTOMER | clear → 204 |

**CartResponse**
```json
{ "items": [ { "productId": 1, "productName": "Mechanical Keyboard", "unitPrice": 499.50,
               "quantity": 2, "subTotal": 999.00 } ],
  "totalItems": 2, "totalAmount": 999.00 }
```

## Orders — `order-service`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/orders` | CUSTOMER | checkout the cart → 201 order (`PENDING`) |
| GET | `/api/orders?page=&size=` | CUSTOMER | own orders, newest first |
| GET | `/api/orders/{id}` | CUSTOMER | own order (404 for others) |
| POST | `/api/orders/{id}/cancel` | CUSTOMER | cancel own PENDING/CONFIRMED order (stock released, refund) |
| GET | `/api/orders/admin?page=&size=` | ADMIN | all orders |
| PATCH | `/api/orders/{id}/status` | ADMIN | `{"status":"SHIPPED"|"DELIVERED"|"CANCELLED"|"CONFIRMED"}` (state machine enforced) |

**OrderResponse**
```json
{ "id": 42, "userId": "8d0c...-keycloak-sub", "status": "CONFIRMED", "statusReason": "Payment received",
  "totalAmount": 999.00,
  "items": [ { "id": 7, "productId": 1, "productName": "Mechanical Keyboard", "quantity": 2,
               "price": 499.50, "subTotal": 999.00 } ],
  "shippingAddress": { "street": "MG Road 1", "city": "Pune", "state": "MH", "country": "India", "zipcode": "411001" },
  "createdAt": "2026-11-15T10:05:00", "updatedAt": "2026-11-15T10:05:01" }
```

## Payments — `payment-service`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/payments/orders/{orderId}` | CUSTOMER | payment of own order (404 for others / not yet processed) |
| GET | `/api/payments/admin?page=&size=` | ADMIN | all payments |

**PaymentResponse**
```json
{ "id": 3, "orderId": 42, "userId": "8d0c...", "amount": 999.00, "status": "COMPLETED",
  "transactionRef": "TXN-4F1A2B3C4D5E6", "failureReason": null,
  "createdAt": "2026-11-15T10:05:01", "refundedAt": null }
```
Statuses: `COMPLETED`, `FAILED`, `REFUNDED`, `SKIPPED` (cancelled before payment).

## Platform endpoints

| Path | Access | Description |
|---|---|---|
| `/actuator/health` | public | gateway health (`UP`) |
| `/swagger-ui.html`, `/v3/api-docs/**`, `/docs/{service}/v3/api-docs` | public (disable with `API_DOCS_ENABLED=false`) | API docs |
| `/eureka/main` | ADMIN | Eureka dashboard through the gateway |
| `/actuator/**` (other) | ADMIN | gateway actuator |

## Internal endpoints (service-to-service, never routed by the gateway)

| Service | Method | Path | Caller |
|---|---|---|---|
| product | POST | `/internal/products/stock/reserve` `{items:[{productId,quantity}]}` → 204 / 409 | order |
| product | POST | `/internal/products/stock/release` → 204 | order |
| user | GET | `/internal/users/{keycloakId}` → UserResponse / 404 | order |
