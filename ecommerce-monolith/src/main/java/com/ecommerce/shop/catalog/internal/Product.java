package com.ecommerce.shop.catalog.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    private String category;
    private String subCategory;

    /** comma separated, e.g. "S,M,L"; empty = product without sizes */
    @Column(name = "sizes")
    private String sizesCsv;

    @Column(nullable = false)
    private Boolean bestseller = false;

    /** main image (first of {@link #images}) */
    private String imageUrl;

    @BatchSize(size = 50)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    @Column(name = "url", nullable = false)
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    List<String> sizes() {
        return sizesCsv == null || sizesCsv.isBlank() ? List.of()
                : Arrays.stream(sizesCsv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    void setSizes(List<String> sizes) {
        this.sizesCsv = sizes == null || sizes.isEmpty() ? null : String.join(",", sizes.stream().map(String::trim).toList());
    }

    List<String> allImages() {
        if (!images.isEmpty()) {
            return List.copyOf(images);
        }
        return imageUrl == null ? List.of() : List.of(imageUrl);
    }
}
