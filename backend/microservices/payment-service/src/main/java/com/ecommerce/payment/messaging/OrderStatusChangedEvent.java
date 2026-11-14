package com.ecommerce.payment.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Consumed from "order.status"; only CANCELLED matters here (refunds). */
public record OrderStatusChangedEvent(Long orderId, String userId, String customerEmail, String customerName,
                                      String status, String previousStatus, BigDecimal totalAmount,
                                      String reason, LocalDateTime occurredAt) {
}
