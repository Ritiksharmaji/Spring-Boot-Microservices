package com.app_ecom.dto;

import java.util.List;

/** Body for product-service /internal/products/stock/{reserve|release}. */
public record StockRequest(List<StockItem> items) {
}
