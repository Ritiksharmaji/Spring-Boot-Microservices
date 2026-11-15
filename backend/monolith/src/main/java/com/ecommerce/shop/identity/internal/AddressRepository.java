package com.ecommerce.shop.identity.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByDefaultAddressDescIdAsc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findFirstByUserIdAndDefaultAddressTrue(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Address a WHERE a.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
