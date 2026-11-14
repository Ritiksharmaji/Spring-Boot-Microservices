package com.ecommerce.shop.identity;

/** What other modules may know about a customer. */
public record Customer(long id, String email, String fullName, DeliveryAddress address) {

    public record DeliveryAddress(String street, String city, String state, String country, String zipcode) {
    }
}
