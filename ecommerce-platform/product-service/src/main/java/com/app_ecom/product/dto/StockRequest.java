package com.app_ecom.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Internal request sent by order-service to reserve or release stock for an order. */
public record StockRequest(@NotEmpty List<@Valid StockItem> items) {
}
