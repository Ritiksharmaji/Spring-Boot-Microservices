package com.ecommerce.shop.ordering.internal;

import com.ecommerce.shop.ordering.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

final class OrderingDtos {

    private OrderingDtos() {
    }

    record AddToCartRequest(@NotNull(message = "productId is required") Long productId,
                            @NotNull @Min(value = 1, message = "quantity must be at least 1")
                            @Max(value = 100, message = "quantity cannot exceed 100") Integer quantity) {
    }

    record QuantityRequest(@NotNull @Min(value = 1, message = "quantity must be at least 1")
                           @Max(value = 100, message = "quantity cannot exceed 100") Integer quantity) {
    }

    record StatusRequest(@NotNull(message = "status is required") OrderStatus status) {
    }

    record CartLine(Long productId, String productName, BigDecimal unitPrice, Integer quantity, BigDecimal subTotal) {
    }

    record CartResponse(List<CartLine> items, int totalItems, BigDecimal totalAmount) {
    }

    record OrderLine(Long productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subTotal) {
    }

    record Address(String street, String city, String state, String country, String zipcode) {
    }

    record OrderResponse(Long id, Long userId, OrderStatus status, String statusReason, BigDecimal totalAmount,
                         List<OrderLine> items, Address shippingAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {

        static OrderResponse from(Order o) {
            ShippingAddress s = o.getShippingAddress();
            return new OrderResponse(o.getId(), o.getUserId(), o.getStatus(), o.getStatusReason(), o.getTotalAmount(),
                    o.getItems().stream().map(i -> new OrderLine(i.getProductId(), i.getProductName(), i.getQuantity(),
                            i.getUnitPrice(), i.subTotal())).toList(),
                    s == null ? null : new Address(s.getStreet(), s.getCity(), s.getState(), s.getCountry(), s.getZipcode()),
                    o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
