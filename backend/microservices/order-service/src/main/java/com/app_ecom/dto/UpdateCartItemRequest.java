package com.app_ecom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 100, message = "quantity cannot exceed 100") Integer quantity) {
}
