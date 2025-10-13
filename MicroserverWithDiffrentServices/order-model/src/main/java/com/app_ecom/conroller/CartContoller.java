package com.app_ecom.conroller;


import com.app_ecom.dto.CartItemRequest;
import com.app_ecom.model.CartItem;
import com.app_ecom.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartContoller {

    private  final CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(@RequestHeader("X-User-ID") String userId,
                                            @RequestBody CartItemRequest request){
        if(!cartService.addToCart(Long.valueOf(userId), request)){
            return ResponseEntity.badRequest().body("Product out of stock or ");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String productId) {
        boolean deleted = cartService.deleteItemFromCart(Long.valueOf(userId), Long.valueOf(productId));
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }
}
