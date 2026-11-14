package com.app_ecom.user;

import com.app_ecom.user.config.MongoConfig;
import com.app_ecom.user.model.User;
import com.app_ecom.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Real MongoDB (Testcontainers): unique indexes and auditing. Skipped without Docker. */
@DataMongoTest
@Import(MongoConfig.class) // slices skip @Configuration classes; auditing lives there
@Testcontainers(disabledWithoutDocker = true)
class UserRepositoryMongoContainerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired UserRepository repository;

    private User user(String keycloakId, String email) {
        User u = new User();
        u.setKeycloakId(keycloakId);
        u.setFirstName("Test");
        u.setEmail(email);
        return u;
    }

    @Test
    void emailIsUniqueAndAuditingFillsTimestamps() {
        User saved = repository.save(user("kc-1", "same@example.com"));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(repository.findByKeycloakId("kc-1")).isPresent();
        assertThat(repository.findByEmailIgnoreCase("SAME@example.com")).isPresent();

        assertThatThrownBy(() -> repository.save(user("kc-2", "same@example.com")))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
