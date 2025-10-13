package com.app_ecom.user.model;

import com.app_ecom.user.dto.UserRole;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data

@Document(collection = "user")
@Collation("en_US") // example: case-insensitive collation for English/US
public class User {

    @Id
    private String id;
    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;
    private String phone;
    private UserRole role = UserRole.CUSTOMER;
    private  Address address;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    // default constructer it is needed for JPA to create entity clas
    // without it jPA can't create any instance of class
    public User() {
    }

    public User(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }


}
