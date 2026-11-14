package com.app_ecom.dto;

import com.app_ecom.model.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(Long id, String userId, OrderStatus status, BigDecimal totalAmount,
                            List<OrderItemDTO> items, AddressDTO shippingAddress,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
}
