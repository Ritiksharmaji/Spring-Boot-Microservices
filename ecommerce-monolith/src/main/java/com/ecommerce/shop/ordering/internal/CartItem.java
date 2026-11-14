package com.ecommerce.shop.ordering.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    /** '' for products without sizes */
    @Column(nullable = false)
    private String size = "";

    @Column(nullable = false)
    private Integer quantity;

    /** unit price when added; checkout always uses the live price */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
}
