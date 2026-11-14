package com.app_ecom.product.repository;

import com.app_ecom.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

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

    /**
     * Atomically decrements stock only if enough is available.
     * Returns 0 when the product is missing, inactive, or short on stock,
     * so concurrent orders can never oversell.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :id AND p.active = true AND p.stockQuantity >= :quantity
            """)
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
