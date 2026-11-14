package com.ecommerce.payment.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Published on "payment.result" (consumed by order-service). */
public record PaymentResultEvent(Long orderId, String userId, Long paymentId, Status status,
                                 BigDecimal amount, String reason, LocalDateTime processedAt) {

    public enum Status { COMPLETED, FAILED }
}
