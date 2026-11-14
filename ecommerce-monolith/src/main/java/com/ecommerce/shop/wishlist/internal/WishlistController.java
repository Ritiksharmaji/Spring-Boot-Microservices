package com.ecommerce.shop.wishlist.internal;

import com.ecommerce.shop.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Wishlist", description = "Products saved for later (returns the product ids)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
class WishlistController {

    private final WishlistService wishlist;

    record WishlistResponse(List<Long> productIds) {
    }

    @Operation(summary = "My wishlist")
    @GetMapping
    WishlistResponse get(@AuthenticationPrincipal Jwt jwt) {
        return new WishlistResponse(wishlist.productIds(CurrentUser.id(jwt)));
    }

    @Operation(summary = "Add a product (idempotent)")
    @PutMapping("/{productId}")
    WishlistResponse add(@AuthenticationPrincipal Jwt jwt, @PathVariable long productId) {
        return new WishlistResponse(wishlist.add(CurrentUser.id(jwt), productId));
    }

    @Operation(summary = "Remove a product")
    @DeleteMapping("/{productId}")
    WishlistResponse remove(@AuthenticationPrincipal Jwt jwt, @PathVariable long productId) {
        return new WishlistResponse(wishlist.remove(CurrentUser.id(jwt), productId));
    }
}
