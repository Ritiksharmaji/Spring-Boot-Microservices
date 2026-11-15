package com.ecommerce.shop.catalog.internal;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
            @Size(max = 100) String subCategory,
            @Size(max = 10, message = "at most 10 sizes") List<@NotBlank @Size(max = 10) String> sizes,
            Boolean bestseller,
            @Size(max = 500) String imageUrl,
            @Size(max = 8, message = "at most 8 images") List<@NotBlank @Size(max = 500) String> images,
            Boolean active) {
    }

    record ProductResponse(Long id, String name, String description, BigDecimal price, Integer stockQuantity,
                           String category, String subCategory, List<String> sizes, Boolean bestseller,
                           String imageUrl, List<String> images, Boolean active,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {

        static ProductResponse from(Product p) {
            List<String> images = p.allImages();
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity(),
                    p.getCategory(), p.getSubCategory(), p.sizes(), p.getBestseller(),
                    images.isEmpty() ? null : images.getFirst(), images, p.getActive(), p.getCreatedAt(), p.getUpdatedAt());
        }
    }

    record ImageUploadResponse(String url) {
    }
}
