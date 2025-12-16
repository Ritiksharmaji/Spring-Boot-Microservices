package com.app_ecom.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
    private String userId;

//    @ManyToOne
//    @JoinColumn(name = "product_id", nullable = false)
    //private Product product;
    private  Long productId;

    private  Integer quantity;
    private BigDecimal price;

    @CreationTimestamp
    private LocalDateTime createAt;
    @UpdateTimestamp
    private LocalDateTime updateAt;
//    @PrePersist
//    protected void onCreate() {
//        System.out.println("PRE PERSIST CALLED");
//        this.createAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
//        this.updateAt = this.createAt;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        System.out.println("PRE UPDATE CALLED");
//        this.updateAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
//    }
//


//    @Column(nullable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @Column(nullable = false)
//    private LocalDateTime updatedAt = LocalDateTime.now();


}
