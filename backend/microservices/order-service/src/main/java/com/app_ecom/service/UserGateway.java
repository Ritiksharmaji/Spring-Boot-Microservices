package com.app_ecom.service;

import com.app_ecom.clients.UserServiceClient;
import com.app_ecom.dto.UserResponse;
import com.app_ecom.exception.BusinessException;
import com.app_ecom.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

/** Resilient access to user-service. */
@Component
@RequiredArgsConstructor
public class UserGateway {

    private static final String CB = "userService";

    private final UserServiceClient client;

    @Retry(name = CB)
    @CircuitBreaker(name = CB, fallbackMethod = "getUserFallback")
    public Optional<UserResponse> getUser(String keycloakId) {
        try {
            return Optional.ofNullable(client.getUserByKeycloakId(keycloakId));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    public Optional<UserResponse> getUserFallback(String keycloakId, Throwable t) {
        if (t instanceof BusinessException be) {
            throw be;
        }
        throw new ServiceUnavailableException("User service is unavailable, please try again later", t);
    }
}
