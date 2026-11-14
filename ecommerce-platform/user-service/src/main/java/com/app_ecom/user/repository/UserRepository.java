package com.app_ecom.user.repository;

import com.app_ecom.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByEmailIgnoreCase(String email);
}
