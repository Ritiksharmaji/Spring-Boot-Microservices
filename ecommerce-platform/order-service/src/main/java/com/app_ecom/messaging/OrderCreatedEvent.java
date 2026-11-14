package com.app_ecom.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Published on "order.created" when a customer places an order (consumed by payment-service). */
public record OrderCreatedEvent(Long orderId, String userId, String customerEmail, String customerName,
                                BigDecimal totalAmount, List<Item> items, LocalDateTime createdAt) {

    public record Item(Long productId, String productName, Integer quantity, BigDecimal price) {
    }
}
