package com.ecommerce.shop.catalog.internal;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

final class ProductDtos {

    private ProductDtos() {
    }

    record ProductRequest(
            @NotBlank(message = "name is required") @Size(max = 150) String name,
            @Size(max = 255) String description,
            @NotNull(message = "price is required") @DecimalMin(value = "0.01", message = "price must be greater than 0")
            @Digits(integer = 10, fraction = 2) BigDecimal price,
            @NotNull(message = "stockQuantity is required") @Min(value = 0, message = "stockQuantity cannot be negative")
            Integer stockQuantity,
            @Size(max = 100) String category,
            @Size(max = 500) String imageUrl,
            Boolean active) {
    }

    record ProductResponse(Long id, String name, String description, BigDecimal price, Integer stockQuantity,
                           String category, String imageUrl, Boolean active,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {

        static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity(),
                    p.getCategory(), p.getImageUrl(), p.getActive(), p.getCreatedAt(), p.getUpdatedAt());
        }
    }
}
