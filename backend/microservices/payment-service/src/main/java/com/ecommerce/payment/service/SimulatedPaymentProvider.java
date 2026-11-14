package com.ecommerce.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic stand-in for a real provider: approves any amount up to
 * {@code app.payment.max-amount} and declines larger ones, so both saga paths can be demonstrated.
 */
@Slf4j
@Component
public class SimulatedPaymentProvider implements PaymentProvider {

    private final BigDecimal maxAmount;

    public SimulatedPaymentProvider(@Value("${app.payment.max-amount:50000.00}") BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public Result charge(Long orderId, BigDecimal amount) {
        if (amount.compareTo(maxAmount) > 0) {
            return new Result(false, null, "Amount " + amount + " exceeds the card limit of " + maxAmount);
        }
        return new Result(true, "TXN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase(), null);
    }

    @Override
    public void refund(String transactionRef, BigDecimal amount) {
        log.info("Refunded {} for transaction {}", amount, transactionRef);
    }
}
