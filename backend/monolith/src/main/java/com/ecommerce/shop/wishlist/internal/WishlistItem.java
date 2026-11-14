package com.ecommerce.shop.wishlist.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items")
@IdClass(WishlistItem.Key.class)
@Getter
@Setter
@NoArgsConstructor
class WishlistItem {

    record Key(Long userId, Long productId) implements Serializable {
        Key() {
            this(null, null);
        }
    }

    @Id
    private Long userId;

    @Id
    private Long productId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    WishlistItem(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
