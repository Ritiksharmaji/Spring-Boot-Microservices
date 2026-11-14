package com.ecommerce.shop.payment.internal;

enum PaymentStatus {
    /** card charged, or cash collected on delivery */
    COMPLETED,
    FAILED,
    REFUNDED,
    /** order cancelled before payment: never charge it */
    SKIPPED,
    /** cash on delivery, not collected yet */
    CASH_ON_DELIVERY
}
