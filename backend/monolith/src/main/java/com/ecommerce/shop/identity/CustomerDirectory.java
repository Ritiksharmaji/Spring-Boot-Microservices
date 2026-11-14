package com.ecommerce.shop.identity;

import java.util.Optional;

/** Public API of the identity module. */
public interface CustomerDirectory {

    Optional<Customer> findCustomer(long userId);
}
