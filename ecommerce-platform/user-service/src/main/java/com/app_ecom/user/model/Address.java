package com.app_ecom.user.model;

import lombok.Data;

/** Embedded inside the user document. */
@Data
public class Address {
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
