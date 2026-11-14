package com.app_ecom.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** One product line in a stock reservation / release request. */
public record StockItem(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity) {
}
