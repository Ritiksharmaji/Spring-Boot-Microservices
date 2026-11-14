package com.ecommerce.shop.shared.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(
        @NotBlank @Size(min = 32, message = "JWT_SECRET must be at least 32 characters") String jwtSecret,
        Duration tokenTtl,
        String issuer) {
}
