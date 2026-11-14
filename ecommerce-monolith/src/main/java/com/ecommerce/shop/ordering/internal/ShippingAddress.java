package com.ecommerce.shop.ordering.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Address snapshot taken at checkout. */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
class ShippingAddress {
    @Column(name = "ship_street") private String street;
    @Column(name = "ship_city") private String city;
    @Column(name = "ship_state") private String state;
    @Column(name = "ship_country") private String country;
    @Column(name = "ship_zipcode") private String zipcode;
}
