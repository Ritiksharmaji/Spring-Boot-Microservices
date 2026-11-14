/**
 * Payment: charges orders after checkout (listens to OrderPlaced), refunds cancelled orders
 * (listens to OrderStatusChanged) and reports outcomes through ordering's OrderPayments API.
 */
@ApplicationModule(displayName = "Payment")
package com.ecommerce.shop.payment;

import org.springframework.modulith.ApplicationModule;
