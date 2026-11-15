package com.ecommerce.shop.ordering.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByIdAsc(Long userId);

    Optional<CartItem> findByUserIdAndProductIdAndSize(Long userId, Long productId, String size);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

interface OrderRepository extends JpaRepository<Order, Long> {

    /** Account deletion: past orders keep the delivery details but lose the customer's contact. */
    @Modifying
    @Query("UPDATE Order o SET o.customerEmail = :email, o.customerName = 'Deleted user' WHERE o.userId = :userId")
    int anonymiseCustomer(@Param("userId") Long userId, @Param("email") String email);


    Page<Order> findByUserId(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
