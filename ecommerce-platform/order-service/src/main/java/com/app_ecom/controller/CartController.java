package com.app_ecom.controller;

import com.app_ecom.dto.CartItemRequest;
import com.app_ecom.dto.CartResponse;
import com.app_ecom.dto.UpdateCartItemRequest;
import com.app_ecom.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.app_ecom.controller.CurrentUser.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@RequestHeader(USER_ID) String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestHeader(USER_ID) String userId,
                                                @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(userId, request));
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@RequestHeader(USER_ID) String userId,
                                   @PathVariable Long productId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(userId, productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@RequestHeader(USER_ID) String userId, @PathVariable Long productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader(USER_ID) String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
