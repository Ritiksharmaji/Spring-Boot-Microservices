/**
 * Ordering: cart, checkout and the order lifecycle.
 * Publishes {@link com.ecommerce.shop.ordering.OrderPlaced} and {@link com.ecommerce.shop.ordering.OrderStatusChanged};
 * receives payment outcomes through {@link com.ecommerce.shop.ordering.OrderPayments}.
 */
@ApplicationModule(displayName = "Ordering")
package com.ecommerce.shop.ordering;

import org.springframework.modulith.ApplicationModule;
