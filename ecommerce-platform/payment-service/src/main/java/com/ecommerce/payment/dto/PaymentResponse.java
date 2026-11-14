package com.ecommerce.payment.dto;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long orderId, String userId, BigDecimal amount, PaymentStatus status,
                              String transactionRef, String failureReason, LocalDateTime createdAt,
                              LocalDateTime refundedAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getUserId(), p.getAmount(), p.getStatus(),
                p.getTransactionRef(), p.getFailureReason(), p.getCreatedAt(), p.getRefundedAt());
    }
}
