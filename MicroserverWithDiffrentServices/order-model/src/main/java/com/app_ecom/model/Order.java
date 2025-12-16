package com.app_ecom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
   // private  User user;
    private Long userId;


    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade= CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

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
//@Column(nullable = false)
//private LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
//
//    @Column(nullable = false)
//    private LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);


}
