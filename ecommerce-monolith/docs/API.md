# API

Base URL: `https://shop.onekampus.com` (live) · `http://localhost:8086` (local).
Interactive: `/swagger-ui.html` — click **Authorize** and paste the `accessToken` from login.
Errors: RFC 7807 `application/problem+json` (`title`, `status`, `detail`, `errors` for validation).

## Auth

```bash
curl -s -X POST https://shop.onekampus.com/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"customer@shop.demo","password":"Customer@123"}'
# {"accessToken":"eyJ…","tokenType":"Bearer","expiresIn":7200,"user":{"id":2,"role":"CUSTOMER",…}}
```

| Method | Path | Access | Body / notes |
|---|---|---|---|
| POST | `/api/auth/register` | public | `{firstName, lastName?, email, password}` (8–72 chars, letter + digit) → 201 token; 409 email taken |
| POST | `/api/auth/login` | public | `{email, password}` → token; 401 wrong; 429 after 5 failures (15 min) |

## Users

| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/api/users/me` | signed in | own profile |
| PUT | `/api/users/me` | signed in | `{firstName, lastName?, phone?, address?{street, city, state?, country, zipcode}}` — email/role not changeable |
| GET | `/api/users?page=&size=` | ADMIN | all users |
| GET | `/api/users/{id}` | ADMIN | one user |
| PUT | `/api/users/{id}/role` | ADMIN | `{role: "CUSTOMER" | "ADMIN"}` |

## Products

| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/api/products?category=&page=&size=&sort=` | public | paged `{content, page:{size, number, totalElements, totalPages}}` |
| GET | `/api/products/{id}` | public | 404 if missing / deleted |
| GET | `/api/products/search?keyword=` | public | in-stock, name or description |
| POST | `/api/products` | ADMIN | `{name, description?, price > 0, stockQuantity ≥ 0, category?, imageUrl?, active?}` → 201 |
| PUT | `/api/products/{id}` | ADMIN | same body |
| DELETE | `/api/products/{id}` | ADMIN | soft delete → 204 |

## Cart

| Method | Path | Notes |
|---|---|---|
| GET | `/api/cart` | `{items:[{productId, productName, unitPrice, quantity, subTotal}], totalItems, totalAmount}` |
| POST | `/api/cart/items` | `{productId, quantity 1–100}` → 201; merges; 409 if more than stock; 404 unknown product |
| PUT | `/api/cart/items/{productId}` | `{quantity}` |
| DELETE | `/api/cart/items/{productId}` · `/api/cart` | remove line · empty cart |

## Orders

| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/api/orders` | signed in | checkout → 201 `PENDING`; 422 empty cart / no address / product gone; 409 stock |
| GET | `/api/orders?page=&size=` | signed in | own orders, newest first |
| GET | `/api/orders/{id}` | signed in | own order (404 for others) |
| POST | `/api/orders/{id}/cancel` | signed in | PENDING/CONFIRMED only (422 otherwise); stock returned, payment refunded |
| GET | `/api/orders/admin` | ADMIN | all orders |
| PATCH | `/api/orders/{id}/status` | ADMIN | `{status}` following the state machine (422 for invalid moves) |

Order: `{id, userId, status, statusReason, totalAmount, items:[{productId, productName, quantity, unitPrice, subTotal}], shippingAddress, createdAt, updatedAt}`.

## Payments

| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/api/payments/orders/{orderId}` | signed in | own order's payment: `COMPLETED`, `FAILED`, `REFUNDED`, `SKIPPED` |
| GET | `/api/payments/admin` | ADMIN | all payments |

## Operations

| Path | Access |
|---|---|
| `/actuator/health`, `/actuator/info` | public |
| `/actuator/modulith` | ADMIN — module structure at runtime |
