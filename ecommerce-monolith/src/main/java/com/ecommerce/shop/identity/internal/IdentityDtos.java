package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.identity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Request/response bodies of the identity module. */
final class IdentityDtos {

    private IdentityDtos() {
    }

    record RegisterRequest(
            @NotBlank(message = "firstName is required") @Size(max = 50) String firstName,
            @Size(max = 50) String lastName,
            @NotBlank(message = "email is required") @Email(message = "email must be valid") @Size(max = 255) String email,
            @NotBlank(message = "password is required")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                    message = "password must be 8-72 characters with at least one letter and one digit") String password) {
    }

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    record TokenResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {
    }

    record AddressDto(
            @NotBlank @Size(max = 200) String street,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 100) String state,
            @NotBlank @Size(max = 100) String country,
            @NotBlank @Size(max = 20) String zipcode) {
    }

    record ProfileRequest(
            @NotBlank(message = "firstName is required") @Size(max = 50) String firstName,
            @Size(max = 50) String lastName,
            @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phone must be 7-15 digits") String phone,
            @Valid AddressDto address) {
    }

    record RoleRequest(Role role) {
    }

    /** Address book entry (POST/PUT /api/addresses). */
    record AddressRequest(
            @Size(max = 30) String label,
            @NotBlank(message = "firstName is required") @Size(max = 50) String firstName,
            @Size(max = 50) String lastName,
            @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phone must be 7-15 digits") String phone,
            @NotBlank(message = "street is required") @Size(max = 200) String street,
            @NotBlank(message = "city is required") @Size(max = 100) String city,
            @Size(max = 100) String state,
            @NotBlank(message = "zipcode is required") @Size(max = 20) String zipcode,
            @NotBlank(message = "country is required") @Size(max = 100) String country,
            Boolean isDefault) {
    }

    record AddressResponse(Long id, String label, String firstName, String lastName, String phone, String street,
                           String city, String state, String zipcode, String country, boolean isDefault) {

        static AddressResponse from(Address a) {
            return new AddressResponse(a.getId(), a.getLabel(), a.getFirstName(), a.getLastName(), a.getPhone(),
                    a.getStreet(), a.getCity(), a.getState(), a.getZipcode(), a.getCountry(), a.isDefaultAddress());
        }
    }

    record UserResponse(Long id, String email, String firstName, String lastName, String phone, Role role,
                        AddressDto address, LocalDateTime createdAt) {

        /** {@code address} = the default entry of the address book (null if none). */
        static UserResponse from(User u, Address defaultAddress) {
            AddressDto address = defaultAddress == null ? null : new AddressDto(defaultAddress.getStreet(),
                    defaultAddress.getCity(), defaultAddress.getState(), defaultAddress.getCountry(), defaultAddress.getZipcode());
            return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getPhone(),
                    u.getRole(), address, u.getCreatedAt());
        }
    }
}
