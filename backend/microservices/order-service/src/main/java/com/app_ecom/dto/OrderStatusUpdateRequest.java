package com.app_ecom.dto;

import com.app_ecom.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull(message = "status is required") OrderStatus status) {
}
