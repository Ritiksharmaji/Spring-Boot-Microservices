package com.app_ecom.user.service;

import com.app_ecom.user.dto.AddressDTO;
import com.app_ecom.user.dto.UserRequest;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.dto.UserRole;
import com.app_ecom.user.exception.DuplicateResourceException;
import com.app_ecom.user.exception.ResourceNotFoundException;
import com.app_ecom.user.model.Address;
import com.app_ecom.user.model.User;
import com.app_ecom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ---------- admin operations ----------

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUser(String id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found")));
    }

    public UserResponse createUser(UserRequest request) {
        ensureEmailFree(request.getEmail(), null);
        User user = new User();
        apply(user, request);
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return toResponse(userRepository.save(user));
    }

    public UserResponse updateUser(String id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        ensureEmailFree(request.getEmail(), user.getId());
        apply(user, request);
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return toResponse(userRepository.save(user));
    }

    // ---------- self-service (current logged-in user) ----------

    public UserResponse getByKeycloakId(String keycloakId) {
        return toResponse(findByKeycloakId(keycloakId));
    }

    /**
     * Creates or updates the profile of the logged-in user.
     * The role is never taken from the request, so customers cannot promote themselves.
     */
    public UserResponse upsertMyProfile(String keycloakId, UserRequest request) {
        User user = userRepository.findByKeycloakId(keycloakId).orElseGet(() -> {
            User created = new User();
            created.setKeycloakId(keycloakId);
            created.setRole(UserRole.CUSTOMER);
            return created;
        });
        ensureEmailFree(request.getEmail(), user.getId());
        apply(user, request);
        return toResponse(userRepository.save(user));
    }

    private User findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found. Create it first with PUT /api/users/me"));
    }

    private void ensureEmailFree(String email, String currentUserId) {
        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Email " + email + " is already registered");
                });
    }

    private void apply(User user, UserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhone(request.getPhone());
        if (request.getAddress() != null) {
            AddressDTO dto = request.getAddress();
            Address address = new Address();
            address.setStreet(dto.getStreet());
            address.setCity(dto.getCity());
            address.setState(dto.getState());
            address.setCountry(dto.getCountry());
            address.setZipcode(dto.getZipcode());
            user.setAddress(address);
        }
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setKeycloakId(user.getKeycloakId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        if (user.getAddress() != null) {
            Address a = user.getAddress();
            AddressDTO dto = new AddressDTO();
            dto.setStreet(a.getStreet());
            dto.setCity(a.getCity());
            dto.setState(a.getState());
            dto.setCountry(a.getCountry());
            dto.setZipcode(a.getZipcode());
            response.setAddress(dto);
        }
        return response;
    }
}
