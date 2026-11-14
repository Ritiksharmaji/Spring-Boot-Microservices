package com.app_ecom.user.controllers;

import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service lookup used by order-service. Not routed by the API gateway. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{keycloakId}")
    public UserResponse getByKeycloakId(@PathVariable String keycloakId) {
        return userService.getByKeycloakId(keycloakId);
    }
}
