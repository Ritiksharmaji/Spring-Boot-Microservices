package com.ecommerce.shop.ordering;


import java.math.BigDecimal;

/** Published on every status change after checkout. */
public record OrderStatusChanged(long orderId, long userId, String customerEmail, String customerName,
                                 OrderStatus status, OrderStatus previousStatus, BigDecimal totalAmount,
                                 String reason) {
}
