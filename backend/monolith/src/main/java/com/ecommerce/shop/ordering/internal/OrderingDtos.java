package com.ecommerce.shop.ordering.internal;

import com.ecommerce.shop.ordering.OrderStatus;
import com.ecommerce.shop.ordering.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

final class OrderingDtos {

    private OrderingDtos() {
    }

    record AddToCartRequest(@NotNull(message = "productId is required") Long productId,
                            @NotNull @Min(value = 1, message = "quantity must be at least 1")
                            @Max(value = 100, message = "quantity cannot exceed 100") Integer quantity,
                            @Size(max = 10) String size) {
    }

    record QuantityRequest(@NotNull @Min(value = 1, message = "quantity must be at least 1")
                           @Max(value = 100, message = "quantity cannot exceed 100") Integer quantity) {
    }

    record StatusRequest(@NotNull(message = "status is required") OrderStatus status) {
    }

    /** Delivery details entered at checkout (optional: the default saved address is used otherwise). */
    record DeliveryAddress(
            @NotBlank(message = "firstName is required") @Size(max = 50) String firstName,
            @Size(max = 50) String lastName,
            @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phone must be 7-15 digits") String phone,
            @NotBlank(message = "street is required") @Size(max = 200) String street,
            @NotBlank(message = "city is required") @Size(max = 100) String city,
            @Size(max = 100) String state,
            @NotBlank(message = "zipcode is required") @Size(max = 20) String zipcode,
            @NotBlank(message = "country is required") @Size(max = 100) String country) {
    }

    record CheckoutRequest(@Valid DeliveryAddress shippingAddress, PaymentMethod paymentMethod,
                           @Size(max = 500) String notes) {
    }

    record CartLine(Long productId, String productName, String size, String imageUrl, BigDecimal unitPrice,
                    Integer quantity, BigDecimal subTotal) {
    }

    record CartResponse(List<CartLine> items, int totalItems, BigDecimal totalAmount) {
    }

    record OrderLine(Long productId, String productName, String size, String imageUrl, Integer quantity,
                     BigDecimal unitPrice, BigDecimal subTotal) {
    }

    record Address(String street, String city, String state, String country, String zipcode) {
    }

    record OrderResponse(Long id, Long userId, OrderStatus status, String statusReason, BigDecimal totalAmount,
                         PaymentMethod paymentMethod, OrderPaymentStatus paymentStatus,
                         List<OrderLine> items, String recipientName, String recipientPhone, Address shippingAddress,
                         String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {

        static OrderResponse from(Order o) {
            ShippingAddress s = o.getShippingAddress();
            return new OrderResponse(o.getId(), o.getUserId(), o.getStatus(), o.getStatusReason(), o.getTotalAmount(),
                    o.getPaymentMethod(), o.getPaymentStatus(),
                    o.getItems().stream().map(i -> new OrderLine(i.getProductId(), i.getProductName(), i.getSize(),
                            i.getImageUrl(), i.getQuantity(), i.getUnitPrice(), i.subTotal())).toList(),
                    o.getRecipientName(), o.getRecipientPhone(),
                    s == null ? null : new Address(s.getStreet(), s.getCity(), s.getState(), s.getCountry(), s.getZipcode()),
                    o.getNotes(), o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
