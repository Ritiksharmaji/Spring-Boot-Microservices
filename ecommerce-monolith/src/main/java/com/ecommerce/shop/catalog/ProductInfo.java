package com.ecommerce.shop.catalog;

import java.math.BigDecimal;
import java.util.List;

/** Product data other modules may use (live price, stock, sizes, main image). */
public record ProductInfo(long id, String name, BigDecimal price, int stockQuantity, List<String> sizes, String imageUrl) {

    /** A size is required when the product has sizes; it must be one of them. */
    public boolean acceptsSize(String size) {
        return sizes.isEmpty() ? size == null || size.isBlank() : sizes.contains(size);
    }
}
