package com.ecommerce.shop.catalog.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true AND p.stockQuantity > 0
              AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    /** Atomic: decrements only if enough stock is left; returns 0 otherwise (no oversell). */
    @Modifying(flushAutomatically = true)  // runs inside the caller's transaction: do not detach its entities
    @Query("""
            UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :id AND p.active = true AND p.stockQuantity >= :quantity
            """)
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true)  // runs inside the caller's transaction: do not detach its entities
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
