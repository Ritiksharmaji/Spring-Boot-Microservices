# API

Base URL: `https://shop-api.onekampus.com` (live) · `http://localhost:8086` (local).
Browser clients: CORS allows `https://shop.onekampus.com` (and `http://localhost:5173` for development).
Interactive: `/swagger-ui.html` — click **Authorize** and paste the `accessToken` from login.
Errors: RFC 7807 `application/problem+json` (`title`, `status`, `detail`, `errors` for validation).

## Auth

```bash
curl -s -X POST https://shop-api.onekampus.com/api/auth/login -H 'Content-Type: application/json' \
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

`PUT /api/users/me` with `address` creates or updates the **default** entry of the address book.

## Address book

| Method | Path | Notes |
|---|---|---|
| GET | `/api/addresses` | own addresses, default first |
| POST | `/api/addresses` | `{label?, firstName, lastName?, phone?, street, city, state?, zipcode, country, isDefault?}` → 201; the first address becomes the default; max 10 |
| PUT | `/api/addresses/{id}` | same body; `isDefault: true` makes it the default (exactly one default) |
| DELETE | `/api/addresses/{id}` | 204; if it was the default, the next one is promoted |

## Wishlist

| Method | Path | Notes |
|---|---|---|
| GET | `/api/wishlist` | `{productIds: [...]}` newest first |
| PUT | `/api/wishlist/{productId}` | add (idempotent); 404 unknown product |
| DELETE | `/api/wishlist/{productId}` | remove |

## Products

| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/api/products?category=&page=&size=&sort=` | public | paged `{content, page:{size, number, totalElements, totalPages}}` |
| GET | `/api/products/{id}` | public | 404 if missing / deleted |
| GET | `/api/products/search?keyword=` | public | in-stock, name or description |
| POST | `/api/products` | ADMIN | `{name, description?, price > 0, stockQuantity ≥ 0, category?, subCategory?, sizes?[], bestseller?, images?[] (≤ 8) or imageUrl?, active?}` → 201 |
| PUT | `/api/products/{id}` | ADMIN | same body |
| DELETE | `/api/products/{id}` | ADMIN | soft delete → 204 |

## Cart

| Method | Path | Notes |
|---|---|---|
| GET | `/api/cart` | `{items:[{productId, productName, unitPrice, quantity, subTotal}], totalItems, totalAmount}` |
| POST | `/api/cart/items` | `{productId, quantity 1–100, size?}` → 201; one line per product **and size**; size required (and must match) for products with sizes (400); 409 if all sizes together exceed stock; 404 unknown product |
| PUT | `/api/cart/items/{productId}?size=M` | `{quantity}` |
| DELETE | `/api/cart/items/{productId}?size=M` · `/api/cart` | remove line · empty cart |

## Orders

| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/api/orders` | signed in | checkout → 201 `PENDING`. Optional body `{shippingAddress?: {firstName, lastName?, phone?, street, city, state?, zipcode, country}, paymentMethod?: "CARD" \| "CASH", notes?}` — without an address the default saved one is used. 422 empty cart / no address / product gone; 409 stock |
| GET | `/api/orders?page=&size=` | signed in | own orders, newest first |
| GET | `/api/orders/{id}` | signed in | own order (404 for others) |
| POST | `/api/orders/{id}/cancel` | signed in | PENDING/CONFIRMED only (422 otherwise); stock returned, payment refunded |
| GET | `/api/orders/admin` | ADMIN | all orders |
| PATCH | `/api/orders/{id}/status` | ADMIN | `{status}` following the state machine (422 for invalid moves) |

Order: `{id, userId, status, statusReason, totalAmount, paymentMethod (CARD|CASH), paymentStatus (PENDING|PAID|FAILED|CASH_ON_DELIVERY|REFUNDED|NOT_CHARGED), items:[{productId, productName, size, imageUrl, quantity, unitPrice, subTotal}], recipientName, recipientPhone, shippingAddress, notes, createdAt, updatedAt}`.

Payment methods: **CARD** — simulated charge after commit (approved ≤ ₹50,000, else declined → order
cancelled, stock returned); **CASH** — confirmed immediately, payment marked PAID when the order is DELIVERED.

## Payments

| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/api/payments/orders/{orderId}` | signed in | own order's payment: `COMPLETED`, `FAILED`, `REFUNDED`, `SKIPPED`, `CASH_ON_DELIVERY` |
| GET | `/api/payments/admin` | ADMIN | all payments |

## Operations

| Path | Access |
|---|---|
| `/actuator/health`, `/actuator/info` | public |
| `/actuator/modulith` | ADMIN — module structure at runtime |
