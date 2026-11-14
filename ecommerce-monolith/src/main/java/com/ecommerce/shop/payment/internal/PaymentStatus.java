package com.ecommerce.shop.payment.internal;

enum PaymentStatus {
    COMPLETED, FAILED, REFUNDED,
    /** order cancelled before payment was attempted: never charge it */
    SKIPPED
}
