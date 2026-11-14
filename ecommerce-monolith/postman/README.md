# Postman — modular monolith

Import `E-Commerce-Monolith.postman_collection.json` and one environment:

| Environment | Base URL | Admin |
|---|---|---|
| `Local.postman_environment.json` | http://localhost:8086 | `admin@local.test` / `Admin12345` (as in the README run command) |
| `Live.postman_environment.json` | https://shop-api.onekampus.com | `admin@shop.onekampus.com`, password from `/opt/apps/.env` (`SHOP_ADMIN_PASSWORD`) — fill it in locally, never commit it |

Run **0. Auth** first, then the whole collection (Runner). Folders: auth, security rules, catalog,
profile, cart & checkout (PENDING → CONFIRMED), declined payment (CANCELLED + stock back),
cancel & refund, admin (ship/deliver, state machine, Modulith actuator), isolation.

On the live demo, the admin folders change shared demo data — prefer running them locally.
