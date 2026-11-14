package com.ecommerce.shop.ordering.internal;

import com.ecommerce.shop.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static com.ecommerce.shop.ordering.internal.OrderingDtos.*;

@Tag(name = "Cart", description = "Shopping cart of the signed-in customer")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
class CartController {

    private final CartService cart;

    @Operation(summary = "My cart")
    @GetMapping
    CartResponse view(@AuthenticationPrincipal Jwt jwt) {
        return cart.view(CurrentUser.id(jwt));
    }

    @Operation(summary = "Add a product (merges with an existing line; stock-checked)")
    @PostMapping("/items")
    ResponseEntity<CartResponse> add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cart.add(CurrentUser.id(jwt), request));
    }

    @Operation(summary = "Set the quantity of a line (size: query parameter for products with sizes)")
    @PutMapping("/items/{productId}")
    CartResponse setQuantity(@AuthenticationPrincipal Jwt jwt, @PathVariable long productId,
                             @RequestParam(required = false) String size,
                             @Valid @RequestBody QuantityRequest request) {
        return cart.setQuantity(CurrentUser.id(jwt), productId, size, request.quantity());
    }

    @Operation(summary = "Remove a line (size: query parameter for products with sizes)")
    @DeleteMapping("/items/{productId}")
    ResponseEntity<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable long productId,
                                @RequestParam(required = false) String size) {
        cart.remove(CurrentUser.id(jwt), productId, size);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Empty the cart")
    @DeleteMapping
    ResponseEntity<Void> clear(@AuthenticationPrincipal Jwt jwt) {
        cart.clear(CurrentUser.id(jwt));
        return ResponseEntity.noContent().build();
    }
}

@Tag(name = "Orders", description = "Checkout, order history, cancellation; status changes (ADMIN)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
class OrderController {

    private final OrderService orders;

    @Operation(summary = "Checkout the cart. Body optional: shippingAddress (else the default saved address), "
            + "paymentMethod CARD (simulated, asynchronous) or CASH (cash on delivery), notes")
    @PostMapping
    ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody(required = false) CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orders.checkout(CurrentUser.id(jwt), request));
    }

    @Operation(summary = "My orders, newest first")
    @GetMapping
    Page<OrderResponse> mine(@AuthenticationPrincipal Jwt jwt,
                             @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                             Pageable pageable) {
        return orders.myOrders(CurrentUser.id(jwt), pageable);
    }

    @Operation(summary = "One of my orders")
    @GetMapping("/{orderId}")
    OrderResponse one(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId) {
        return orders.myOrder(CurrentUser.id(jwt), orderId);
    }

    @Operation(summary = "Cancel one of my orders (PENDING or CONFIRMED; stock returned, payment refunded)")
    @PostMapping("/{orderId}/cancel")
    OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId) {
        return orders.cancel(CurrentUser.id(jwt), orderId);
    }

    @Operation(summary = "All orders (ADMIN)")
    @GetMapping("/admin")
    Page<OrderResponse> all(@ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                            Pageable pageable) {
        return orders.allOrders(pageable);
    }

    @Operation(summary = "Change order status, e.g. CONFIRMED -> SHIPPED -> DELIVERED (ADMIN)")
    @PatchMapping("/{orderId}/status")
    OrderResponse status(@PathVariable long orderId, @Valid @RequestBody StatusRequest request) {
        return orders.changeStatus(orderId, request.status());
    }
}
