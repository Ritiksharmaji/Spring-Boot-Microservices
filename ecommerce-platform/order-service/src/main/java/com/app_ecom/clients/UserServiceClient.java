package com.app_ecom.clients;

import com.app_ecom.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** Declarative HTTP client for user-service. */
@HttpExchange
public interface UserServiceClient {

    @GetExchange("/internal/users/{keycloakId}")
    UserResponse getUserByKeycloakId(@PathVariable String keycloakId);
}
