package com.app_ecom.user.controllers;

import com.app_ecom.user.dto.UserRequest;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * X-User-ID is set by the API gateway from the verified JWT ("sub" claim);
 * the gateway strips any value sent by the client.
 * Endpoints other than /me are ADMIN-only (enforced at the gateway).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // ---- current user ----

    @GetMapping("/me")
    public UserResponse getMyProfile(@RequestHeader("X-User-ID") String keycloakId) {
        return userService.getByKeycloakId(keycloakId);
    }

    @PutMapping("/me")
    public UserResponse upsertMyProfile(@RequestHeader("X-User-ID") String keycloakId,
                                        @Valid @RequestBody UserRequest request) {
        return userService.upsertMyProfile(keycloakId, request);
    }

    // ---- admin ----

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable String id) {
        return userService.getUser(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }
}
