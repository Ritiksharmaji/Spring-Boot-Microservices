package com.ecommerce.shop.catalog;

import java.util.List;
import java.util.Optional;

/** Public API of the catalog module. */
public interface Catalog {

    /** Active product with its live price and stock, or empty if missing / deleted. */
    Optional<ProductInfo> findActiveProduct(long productId);

    /**
     * Reserves stock for all lines, all-or-nothing, inside the caller's transaction
     * (one database: the order and the stock change commit or roll back together).
     * Throws a 409 business exception if any line is short.
     */
    void reserve(List<StockLine> lines);

    /** Puts stock back (order cancelled / payment failed). */
    void release(List<StockLine> lines);
}
