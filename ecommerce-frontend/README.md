# E-Commerce Frontend (React)

**Live: https://shop.onekampus.com** — API: https://shop-api.onekampus.com ([Swagger](https://shop-api.onekampus.com/swagger-ui.html))
Demo customer: `customer@shop.demo` / `Customer@123` (or create an account).

React 18 + Vite + Tailwind CSS storefront for the [modular monolith](../ecommerce-monolith) backend.
Originally written for an Express backend (repository `Ecommerce-Application`, folder
`frontend/webapp_ecommerce_app`) and adapted to the Spring Boot API.

## Features

- Home (hero, latest collection, bestsellers), collection with category filters, price filter, sort, search
- Product page with image gallery, **sizes**, stock badge, related products
- **Cart** per product and size — kept in the browser for guests, synced to the server after login (guest items are merged)
- **Wishlist** (guest in the browser, server-side when logged in)
- Login / sign-up (JWT from the API), **profile with address book** (default address, add / edit / delete)
- **Checkout** with delivery details, notes and payment method: **demo card** (approved up to ₹50,000,
  declined above → order cancelled and stock returned) or **cash on delivery**
- **My orders** with live status (auto-refresh while payment is processing: PENDING → CONFIRMED),
  payment status, cancel (refund for paid orders)

## How it talks to the API

All HTTP calls live in [`src/context/ShopContext.jsx`](src/context/ShopContext.jsx) and
[`src/api/`](src/api): `client.js` (axios, bearer token, RFC 7807 error messages) and `mappers.js`
(API JSON ↔ the shapes the components use). Pages never build URLs themselves except through the context.

| UI action | API call |
|---|---|
| catalogue | `GET /api/products?size=500&sort=id,desc`, `GET /api/products/{id}` |
| login / sign-up | `POST /api/auth/login`, `POST /api/auth/register` → `accessToken` |
| profile | `GET /api/users/me` |
| cart | `GET /api/cart`, `POST /api/cart/items`, `PUT`/`DELETE /api/cart/items/{id}?size=`, `DELETE /api/cart` |
| wishlist | `GET /api/wishlist`, `PUT`/`DELETE /api/wishlist/{productId}` |
| address book | `GET`/`POST /api/addresses`, `PUT`/`DELETE /api/addresses/{id}` |
| checkout | `POST /api/orders` `{shippingAddress, paymentMethod: CARD|CASH, notes}` |
| orders | `GET /api/orders`, `POST /api/orders/{id}/cancel` |

Product images live in [`public/products/`](public/products) and the API returns paths like
`/products/p_img1.png`, so the browser loads them from this site.

## Run

```bash
npm ci
npm run dev          # http://localhost:5173  (API at http://localhost:8086, see .env.development)
npm run build        # dist/  (API URL from .env.production)
```

Start the backend first: see [ecommerce-monolith/README.md](../ecommerce-monolith/README.md#run-locally).
The API allows browser calls from `http://localhost:5173` and `https://shop.onekampus.com` (CORS).

## Deploy

`deploy/deploy.sh` builds with `.env.production` and uploads `dist/` to the shared server, where Caddy
serves it at https://shop.onekampus.com (single-page-app fallback to `index.html`). See
[ecommerce-monolith/docs/DEPLOYMENT.md](../ecommerce-monolith/docs/DEPLOYMENT.md).
