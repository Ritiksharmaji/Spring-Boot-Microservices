package com.app_ecom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Snapshot of the delivery address taken when the order is placed. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    @Column(name = "ship_street")
    private String street;
    @Column(name = "ship_city")
    private String city;
    @Column(name = "ship_state")
    private String state;
    @Column(name = "ship_country")
    private String country;
    @Column(name = "ship_zipcode")
    private String zipcode;
}
