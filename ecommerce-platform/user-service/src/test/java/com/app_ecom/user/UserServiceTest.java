package com.app_ecom.user;

import com.app_ecom.user.dto.UserRequest;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.dto.UserRole;
import com.app_ecom.user.exception.DuplicateResourceException;
import com.app_ecom.user.model.User;
import com.app_ecom.user.repository.UserRepository;
import com.app_ecom.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repository;
    @InjectMocks UserService service;

    private UserRequest request(String email, UserRole role) {
        UserRequest r = new UserRequest();
        r.setFirstName("Alice");
        r.setEmail(email);
        r.setRole(role);
        return r;
    }

    @Test
    void customerCannotPromoteThemselvesThroughMyProfile() {
        when(repository.findByKeycloakId("kc-alice")).thenReturn(Optional.empty());
        when(repository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponse saved = service.upsertMyProfile("kc-alice", request("Alice@Example.com", UserRole.ADMIN));

        assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(saved.getKeycloakId()).isEqualTo("kc-alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void emailAlreadyUsedByAnotherAccountIsRejected() {
        User other = new User();
        other.setId("other");
        when(repository.findByKeycloakId("kc-alice")).thenReturn(Optional.empty());
        when(repository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.upsertMyProfile("kc-alice", request("taken@example.com", null)))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
