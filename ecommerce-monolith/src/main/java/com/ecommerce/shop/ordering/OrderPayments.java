package com.ecommerce.shop.ordering;

/**
 * Public API used by the payment module to report the outcome of a charge.
 * (A method call instead of an event keeps module dependencies acyclic: payment -> ordering.)
 * Both methods are idempotent: they only act while the order is still PENDING.
 */
public interface OrderPayments {

    void paymentSucceeded(long orderId);

    void paymentFailed(long orderId, String reason);
}
