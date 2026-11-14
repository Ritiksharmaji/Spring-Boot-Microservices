#!/usr/bin/env bash
# =============================================================================
# End-to-end smoke test against a running stack (docker compose up -d --build).
# Exercises: Keycloak login -> gateway security -> product/user/order/payment/notification
# services -> RabbitMQ saga -> outbox -> email. Exits non-zero on the first failure.
#
#   ./scripts/smoke-test.sh                 # defaults to localhost
#   GATEWAY=http://host:8080 KEYCLOAK=http://host:8483 ./scripts/smoke-test.sh
# Requires: curl, jq
# =============================================================================
set -euo pipefail

GATEWAY=${GATEWAY:-http://localhost:8080}
KEYCLOAK=${KEYCLOAK:-http://localhost:8483}
MAILPIT=${MAILPIT:-http://localhost:8025}
REALM=${REALM:-ecom-app}
CLIENT_ID=${CLIENT_ID:-oauth2-ecom-pkce}
RUN_ID=$(date +%s)

pass() { printf '  \033[32m✔\033[0m %s\n' "$1"; }
fail() { printf '  \033[31m✘ %s\033[0m\n' "$1"; exit 1; }
step() { printf '\n\033[1m%s\033[0m\n' "$1"; }

# wait_for <description> <timeout-seconds> <command...>
wait_for() {
  local what=$1 timeout=$2; shift 2
  local end=$((SECONDS + timeout))
  until "$@" >/dev/null 2>&1; do
    [ $SECONDS -ge $end ] && fail "timed out waiting for $what"
    sleep 3
  done
  pass "$what"
}

token() {
  curl -sf -X POST "$KEYCLOAK/realms/$REALM/protocol/openid-connect/token" \
    -d grant_type=password -d client_id="$CLIENT_ID" -d username="$1" -d password="$2" | jq -r .access_token
}

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

# api <METHOD> <path> <token|-> [json-body]
# Prints the body; the status code goes to a file because callers use $(...) and pipes (subshells).
api() {
  local method=$1 path=$2 tok=$3 body=${4:-}
  local args=(-s -o "$TMP/body" -w '%{http_code}' -X "$method" "$GATEWAY$path")
  [ "$tok" != "-" ] && args+=(-H "Authorization: Bearer $tok")
  [ -n "$body" ] && args+=(-H 'Content-Type: application/json' -d "$body")
  curl "${args[@]}" > "$TMP/status" || echo 000 > "$TMP/status"
  cat "$TMP/body"
}

expect_status() {
  local got; got=$(cat "$TMP/status")
  [ "$got" = "$1" ] || fail "$2: expected HTTP $1, got $got ($(cat "$TMP/body"))"
  pass "$2 ($1)"
}

# ---------------------------------------------------------------------------
step "1. Waiting for the platform"
wait_for "gateway healthy" 420 bash -c "curl -sf $GATEWAY/actuator/health | grep -q UP"
wait_for "keycloak realm ready" 300 curl -sf "$KEYCLOAK/realms/$REALM/.well-known/openid-configuration"
wait_for "product-service routed via Eureka" 300 bash -c "curl -sf -o /dev/null $GATEWAY/api/products"

step "2. Authentication & authorization"
ADMIN=$(token admin 'Admin@123')
{ [ -n "$ADMIN" ] && [ "$ADMIN" != "null" ]; } || fail "admin login"; pass "admin token"
CUSTOMER=$(token customer 'Customer@123')
{ [ -n "$CUSTOMER" ] && [ "$CUSTOMER" != "null" ]; } || fail "customer login"; pass "customer token"
api GET /api/cart - >/dev/null;                       expect_status 401 "anonymous cart is rejected"
api POST /api/products "$CUSTOMER" '{}' >/dev/null;   expect_status 403 "customer cannot create products"
api GET /api/users "$CUSTOMER" >/dev/null;            expect_status 403 "customer cannot list users"
api GET /api/orders/admin "$CUSTOMER" >/dev/null;     expect_status 403 "customer cannot list all orders"

step "3. Catalog (admin)"
PRODUCT=$(api POST /api/products "$ADMIN" "{\"name\":\"Smoke Keyboard $RUN_ID\",\"description\":\"smoke test\",\"price\":499.50,\"stockQuantity\":5,\"category\":\"electronics\"}")
expect_status 201 "admin creates product"
PRODUCT_ID=$(echo "$PRODUCT" | jq -r .id)
EXPENSIVE=$(api POST /api/products "$ADMIN" "{\"name\":\"Smoke Laptop $RUN_ID\",\"price\":60000.00,\"stockQuantity\":1,\"category\":\"electronics\"}")
expect_status 201 "admin creates expensive product (will be declined)"
EXPENSIVE_ID=$(echo "$EXPENSIVE" | jq -r .id)
api GET "/api/products/$PRODUCT_ID" - | jq -e '.price == 499.5' >/dev/null; expect_status 200 "anonymous can read the product"
api POST /api/products "$ADMIN" '{"name":"","price":-1}' | jq -e '.errors.name' >/dev/null; expect_status 400 "validation errors are reported"

step "4. Customer profile"
api PUT /api/users/me "$CUSTOMER" '{"firstName":"Riya","lastName":"Customer","email":"customer@shop.local","phone":"+919876543210","address":{"street":"MG Road 1","city":"Pune","state":"MH","country":"India","zipcode":"411001"}}' >/dev/null
expect_status 200 "customer saves profile with address"

step "5. Happy path: cart -> order -> payment -> confirmed"
api DELETE /api/cart "$CUSTOMER" >/dev/null
api POST /api/cart/items "$CUSTOMER" "{\"productId\":$PRODUCT_ID,\"quantity\":2}" | jq -e '.totalAmount == 999' >/dev/null
expect_status 201 "add 2 x 499.50 to cart (total 999.00)"
api POST /api/cart/items "$CUSTOMER" "{\"productId\":$PRODUCT_ID,\"quantity\":10}" >/dev/null
expect_status 409 "cannot add more than the available stock"
ORDER=$(api POST /api/orders "$CUSTOMER")
expect_status 201 "checkout"
ORDER_ID=$(echo "$ORDER" | jq -r .id)
[ "$(echo "$ORDER" | jq -r .status)" = "PENDING" ] && pass "order $ORDER_ID is PENDING (awaiting payment)"
wait_for "order $ORDER_ID CONFIRMED by the saga" 60 bash -c \
  "curl -sf -H 'Authorization: Bearer $CUSTOMER' $GATEWAY/api/orders/$ORDER_ID | jq -e '.status == \"CONFIRMED\"'"
api GET "/api/payments/orders/$ORDER_ID" "$CUSTOMER" | jq -e '.status == "COMPLETED"' >/dev/null; expect_status 200 "payment COMPLETED"
api GET "/api/products/$PRODUCT_ID" - | jq -e '.stockQuantity == 3' >/dev/null; expect_status 200 "stock reduced 5 -> 3"
wait_for "confirmation email delivered" 60 bash -c \
  "curl -sf '$MAILPIT/api/v1/search?query=subject:%22Order%20%23$ORDER_ID%20confirmed%22' | jq -e '.messages_count >= 1'"

step "6. Admin fulfilment"
api PATCH "/api/orders/$ORDER_ID/status" "$ADMIN" '{"status":"SHIPPED"}' | jq -e '.status == "SHIPPED"' >/dev/null; expect_status 200 "admin ships order"
api POST "/api/orders/$ORDER_ID/cancel" "$CUSTOMER" >/dev/null; expect_status 422 "shipped order cannot be cancelled"
api PATCH "/api/orders/$ORDER_ID/status" "$ADMIN" '{"status":"DELIVERED"}' >/dev/null; expect_status 200 "admin marks delivered"

step "7. Failure path: payment declined -> order cancelled -> stock released"
api POST /api/cart/items "$CUSTOMER" "{\"productId\":$EXPENSIVE_ID,\"quantity\":1}" >/dev/null; expect_status 201 "add expensive item"
FAILED_ORDER_ID=$(api POST /api/orders "$CUSTOMER" | jq -r .id); expect_status 201 "checkout"
wait_for "order $FAILED_ORDER_ID CANCELLED after declined payment" 60 bash -c \
  "curl -sf -H 'Authorization: Bearer $CUSTOMER' $GATEWAY/api/orders/$FAILED_ORDER_ID | jq -e '.status == \"CANCELLED\"'"
wait_for "stock of declined item released (back to 1)" 30 bash -c \
  "curl -sf $GATEWAY/api/products/$EXPENSIVE_ID | jq -e '.stockQuantity == 1'"

step "8. Customer cancellation -> refund"
api POST /api/cart/items "$CUSTOMER" "{\"productId\":$PRODUCT_ID,\"quantity\":1}" >/dev/null
CANCEL_ID=$(api POST /api/orders "$CUSTOMER" | jq -r .id); expect_status 201 "checkout"
wait_for "order $CANCEL_ID CONFIRMED" 60 bash -c \
  "curl -sf -H 'Authorization: Bearer $CUSTOMER' $GATEWAY/api/orders/$CANCEL_ID | jq -e '.status == \"CONFIRMED\"'"
api POST "/api/orders/$CANCEL_ID/cancel" "$CUSTOMER" | jq -e '.status == "CANCELLED"' >/dev/null; expect_status 200 "customer cancels"
wait_for "payment REFUNDED" 60 bash -c \
  "curl -sf -H 'Authorization: Bearer $CUSTOMER' $GATEWAY/api/payments/orders/$CANCEL_ID | jq -e '.status == \"REFUNDED\"'"

step "9. Isolation between customers"
OTHER=$(token customer2 'Customer@123')
api GET "/api/orders/$ORDER_ID" "$OTHER" >/dev/null; expect_status 404 "another customer cannot see the order"

printf '\n\033[32mAll smoke tests passed.\033[0m\n'
