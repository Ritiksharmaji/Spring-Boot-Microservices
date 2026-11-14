package com.app_ecom.product.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final Long productId;

    public InsufficientStockException(Long productId, int requested) {
        super("Insufficient stock for product " + productId + " (requested " + requested + ")");
        this.productId = productId;
    }
}
