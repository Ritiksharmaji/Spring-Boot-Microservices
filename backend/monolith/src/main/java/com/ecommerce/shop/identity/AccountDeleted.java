package com.ecommerce.shop.identity;

/**
 * Published when a customer deletes their account. The user row stays (orders reference it) but is
 * anonymised; listeners remove the customer's personal data in their module (cart, wishlist, order contact).
 */
public record AccountDeleted(long userId, String anonymisedEmail) {
}
