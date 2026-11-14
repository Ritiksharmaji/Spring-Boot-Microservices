package com.app_ecom.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Received on "payment.result" from payment-service. */
public record PaymentResultEvent(Long orderId, String userId, Long paymentId, Status status,
                                 BigDecimal amount, String reason, LocalDateTime processedAt) {

    public enum Status { COMPLETED, FAILED }
}
