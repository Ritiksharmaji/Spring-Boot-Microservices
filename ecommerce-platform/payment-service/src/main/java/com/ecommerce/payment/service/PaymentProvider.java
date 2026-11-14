package com.ecommerce.payment.service;

import java.math.BigDecimal;

/** Abstraction over a real payment provider (Razorpay, Stripe...). */
public interface PaymentProvider {

    Result charge(Long orderId, BigDecimal amount);

    void refund(String transactionRef, BigDecimal amount);

    record Result(boolean success, String transactionRef, String failureReason) {
    }
}
