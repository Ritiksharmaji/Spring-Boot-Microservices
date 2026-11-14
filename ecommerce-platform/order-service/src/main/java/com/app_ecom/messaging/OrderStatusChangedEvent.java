package com.app_ecom.messaging;

import com.app_ecom.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published on "order.status" whenever an order changes state
 * (consumed by notification-service, and by payment-service for refunds).
 */
public record OrderStatusChangedEvent(Long orderId, String userId, String customerEmail, String customerName,
                                      OrderStatus status, OrderStatus previousStatus, BigDecimal totalAmount,
                                      String reason, LocalDateTime occurredAt) {
}
