package com.ecommerce.shop.payment.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated payment provider (swap for Razorpay/Stripe): approves amounts up to
 * app.payment.max-amount and declines larger ones, so both outcomes can be demonstrated.
 */
@Slf4j
@Component
class PaymentProvider {

    record Result(boolean success, String reference, String failureReason) {
    }

    private final BigDecimal maxAmount;

    PaymentProvider(@Value("${app.payment.max-amount:50000.00}") BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    Result charge(long orderId, BigDecimal amount) {
        if (amount.compareTo(maxAmount) > 0) {
            return new Result(false, null, "Amount " + amount + " exceeds the card limit of " + maxAmount);
        }
        return new Result(true, "TXN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase(), null);
    }

    void refund(String reference, BigDecimal amount) {
        log.info("Refunded {} for {}", amount, reference);
    }
}
