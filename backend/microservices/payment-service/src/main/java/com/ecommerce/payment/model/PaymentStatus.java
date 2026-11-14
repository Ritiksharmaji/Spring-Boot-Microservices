package com.ecommerce.payment.model;

public enum PaymentStatus {
    /** Money captured. */
    COMPLETED,
    /** Declined by the provider; the order is cancelled. */
    FAILED,
    /** Money returned after the order was cancelled. */
    REFUNDED,
    /** The order was cancelled before payment was attempted; nothing will be charged. */
    SKIPPED
}
