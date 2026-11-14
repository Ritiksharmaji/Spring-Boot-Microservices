package com.app_ecom.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "firstName is required")
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phone must be 7-15 digits")
    private String phone;

    /** Only honoured on the admin endpoints; ignored for /me. */
    private UserRole role;

    @Valid
    private AddressDTO address;
}
