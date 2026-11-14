package com.app_ecom.controller;

import com.app_ecom.dto.OrderResponse;
import com.app_ecom.dto.OrderStatusUpdateRequest;
import com.app_ecom.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.app_ecom.controller.CurrentUser.ROLES;
import static com.app_ecom.controller.CurrentUser.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /** Checkout: turns the current cart into an order. */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader(USER_ID) String userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(userId));
    }

    @GetMapping
    public Page<OrderResponse> myOrders(@RequestHeader(USER_ID) String userId,
                                        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                                        Pageable pageable) {
        return orderService.getMyOrders(userId, pageable);
    }

    @GetMapping("/{orderId}")
    public OrderResponse myOrder(@RequestHeader(USER_ID) String userId, @PathVariable Long orderId) {
        return orderService.getMyOrder(userId, orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@RequestHeader(USER_ID) String userId, @PathVariable Long orderId) {
        return orderService.cancelMyOrder(userId, orderId);
    }

    // ---- admin ----

    @GetMapping("/admin")
    public Page<OrderResponse> allOrders(@RequestHeader(value = ROLES, required = false) String roles,
                                         @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                         Pageable pageable) {
        CurrentUser.requireAdmin(roles);
        return orderService.getAllOrders(pageable);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@RequestHeader(value = ROLES, required = false) String roles,
                                      @PathVariable Long orderId,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        CurrentUser.requireAdmin(roles);
        return orderService.updateStatus(orderId, request.status());
    }
}
