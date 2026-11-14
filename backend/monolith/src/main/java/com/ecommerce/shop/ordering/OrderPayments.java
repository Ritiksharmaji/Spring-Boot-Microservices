package com.ecommerce.shop.ordering;

/**
 * Public API used by the payment module to report outcomes.
 * (A method call instead of an event keeps module dependencies acyclic: payment -> ordering.)
 * All methods are idempotent.
 */
public interface OrderPayments {

    /** Card charged: PENDING -> CONFIRMED. */
    void paymentSucceeded(long orderId);

    /** Card declined: PENDING -> CANCELLED, stock released. */
    void paymentFailed(long orderId, String reason);

    /** Cash on delivery accepted: PENDING -> CONFIRMED, paid when delivered. */
    void cashOnDeliveryAccepted(long orderId);

    /** Money returned after a cancellation (payment status only). */
    void paymentRefunded(long orderId);
}
