package com.ecommerce.shop.wishlist.internal;

import com.ecommerce.shop.catalog.Catalog;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

interface WishlistRepository extends JpaRepository<WishlistItem, WishlistItem.Key> {

    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}

@Service
@RequiredArgsConstructor
class WishlistService {

    static final int MAX_ITEMS = 100;

    private final WishlistRepository wishlist;
    private final Catalog catalog;

    @Transactional(readOnly = true)
    public List<Long> productIds(long userId) {
        return wishlist.findByUserIdOrderByCreatedAtDesc(userId).stream().map(WishlistItem::getProductId).toList();
    }

    /** Idempotent: adding twice keeps one entry. */
    @Transactional
    public List<Long> add(long userId, long productId) {
        catalog.findActiveProduct(productId)
                .orElseThrow(() -> BusinessException.notFound("Product " + productId + " not found"));
        if (!wishlist.existsById(new WishlistItem.Key(userId, productId))) {
            if (wishlist.countByUserId(userId) >= MAX_ITEMS) {
                throw BusinessException.invalid("Wishlist Full", "Your wishlist can hold up to " + MAX_ITEMS + " products");
            }
            wishlist.save(new WishlistItem(userId, productId));
        }
        return productIds(userId);
    }

    @Transactional
    public List<Long> remove(long userId, long productId) {
        wishlist.deleteById(new WishlistItem.Key(userId, productId));
        return productIds(userId);
    }
}
