package com.app_ecom.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDTO {
    @NotBlank @Size(max = 200) private String street;
    @NotBlank @Size(max = 100) private String city;
    @Size(max = 100) private String state;
    @NotBlank @Size(max = 100) private String country;
    @NotBlank @Size(max = 20) private String zipcode;
}
