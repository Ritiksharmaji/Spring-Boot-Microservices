package com.app_ecom.user.model;

import com.app_ecom.user.dto.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Entity(name = "user_table")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role = UserRole.CUSTOMER;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private  Address address;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // default constructer it is needed for JPA to create entity clas
    // without it jPA can't create any instance of class
    public User() {
    }

    public User(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }


}
