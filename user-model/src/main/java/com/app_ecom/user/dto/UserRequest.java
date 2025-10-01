package com.app_ecom.user.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private AddressDTO address;
}
