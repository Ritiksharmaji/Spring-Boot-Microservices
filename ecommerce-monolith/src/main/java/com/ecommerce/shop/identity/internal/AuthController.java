package com.ecommerce.shop.identity.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ecommerce.shop.identity.internal.IdentityDtos.*;

@Tag(name = "Auth", description = "Register and log in (returns a JWT access token)")
@SecurityRequirements // public endpoints
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
class AuthController {

    private final IdentityService identity;

    @Operation(summary = "Create a customer account and log in")
    @PostMapping("/register")
    ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(identity.register(request));
    }

    @Operation(summary = "Log in with email and password")
    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return identity.login(request);
    }
}
