# Postman Collection

Files in [`../postman`](../postman):

| File | Purpose |
|---|---|
| `E-Commerce-Platform.postman_collection.json` | 12 folders, 52 requests, 74 test assertions |
| `Local.postman_environment.json` | local Docker stack (seeded users, password grant) |
| `Production.postman_environment.json` | template for the deployed API (fill in domains) |

## Use it

1. Start the stack: `docker compose up -d --build` and wait for `docker compose ps` → all healthy.
2. Postman → **Import** → select the collection and the Local environment → choose **Local** (top right).
3. Run folder **0. Auth** — it stores `adminToken`, `customerToken`, `customer2Token`.
4. Click any request, or **Run collection** (Runner) to execute everything top to bottom.
   The run is an end-to-end test: every request asserts its status and key fields, and ids
   (`productId`, `orderId`, …) are passed between requests automatically.

Tokens live 15 minutes — re-run folder 0 when you get 401s.

## Folders

| # | Folder | Shows |
|---|---|---|
| 0 | Auth | Keycloak password grant (local realm only) |
| 1 | Security rules | 401 / 403, spoofed `X-User-ID` ignored |
| 2 | Products (admin) | create, update, validation error |
| 3 | Catalog (public) | paging, category filter, search, 404 |
| 4 | Customer profile | create/update own profile, role cannot be self-assigned, validation |
| 5 | Cart | clear, add, stock check (409), update, view |
| 6 | Checkout (saga happy path) | PENDING → CONFIRMED, payment COMPLETED, stock reduced, email in Mailpit |
| 7 | Payment declined | order CANCELLED with reason, stock released |
| 8 | Cancel & refund | customer cancels, payment REFUNDED |
| 9 | Admin | all orders, ship/deliver, invalid transition (422), payments, users, soft delete |
| 10 | Isolation | other customers get 404 |
| 11 | Platform | health, OpenAPI documents |

Requests marked with a short pre-request delay wait ~2 s for the asynchronous saga
(outbox → RabbitMQ → payment-service → order-service).

## Production

The production realm has **no password grant**. In the Production environment use Postman's
OAuth 2.0 helper on the collection's *Authorization* tab: grant type *Authorization Code (With PKCE)*,
auth URL `https://AUTH_DOMAIN/realms/ecom-app/protocol/openid-connect/auth`, token URL
`…/token`, client id `oauth2-ecom-pkce`, callback `https://oauth.pstmn.io/v1/callback`
(add this callback to the client's redirect URIs in Keycloak while testing, then remove it).

## Command line (Newman)

```bash
npx newman run postman/E-Commerce-Platform.postman_collection.json -e postman/Local.postman_environment.json
```
