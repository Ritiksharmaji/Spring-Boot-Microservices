package com.ecommerce.shop.catalog;

import java.math.BigDecimal;

/** Product data other modules may use (live price and stock). */
public record ProductInfo(long id, String name, BigDecimal price, int stockQuantity) {
}
