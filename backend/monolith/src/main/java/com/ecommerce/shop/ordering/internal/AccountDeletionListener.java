package com.ecommerce.shop.ordering.internal;

import com.ecommerce.shop.identity.AccountDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Removes a deleted customer's cart and contact details from their orders. */
@Slf4j
@Component
@RequiredArgsConstructor
class AccountDeletionListener {

    private final CartItemRepository cart;
    private final OrderRepository orders;

    @ApplicationModuleListener
    void on(AccountDeleted event) {
        cart.deleteAllByUserId(event.userId());
        int updated = orders.anonymiseCustomer(event.userId(), event.anonymisedEmail());
        log.info("Account {} deleted: cart cleared, {} order(s) anonymised", event.userId(), updated);
    }
}
