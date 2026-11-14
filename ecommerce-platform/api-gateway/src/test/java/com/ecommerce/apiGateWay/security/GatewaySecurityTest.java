package com.ecommerce.apiGateWay.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/** Verifies the access rules at the gateway (no downstream services needed). */
@SpringBootTest
@AutoConfigureWebTestClient
class GatewaySecurityTest {

    @Autowired WebTestClient client;

    @MockitoBean ReactiveJwtDecoder jwtDecoder; // tokens are mocked with mockJwt()

    private WebTestClient as(String role) {
        return client.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void anonymousCannotUseCartOrOrders() {
        client.get().uri("/api/cart").exchange().expectStatus().isUnauthorized();
        client.post().uri("/api/orders").exchange().expectStatus().isUnauthorized();
        client.get().uri("/api/users/me").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void customersCannotUseAdminEndpoints() {
        as("CUSTOMER").post().uri("/api/products").contentType(MediaType.APPLICATION_JSON).bodyValue("{}")
                .exchange().expectStatus().isForbidden();
        as("CUSTOMER").delete().uri("/api/products/1").exchange().expectStatus().isForbidden();
        as("CUSTOMER").get().uri("/api/users").exchange().expectStatus().isForbidden();
        as("CUSTOMER").get().uri("/api/orders/admin").exchange().expectStatus().isForbidden();
        as("CUSTOMER").patch().uri("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"SHIPPED\"}").exchange().expectStatus().isForbidden();
    }

    @Test
    void unknownAndInternalPathsAreClosedEvenForAdmins() {
        as("ADMIN").post().uri("/internal/products/stock/reserve").exchange().expectStatus().isForbidden();
        as("ADMIN").get().uri("/something-else").exchange().expectStatus().isForbidden();
    }

    @Test
    void customerPassesSecurityForCartAndProfile() {
        // Security lets the request through; with no order-service running the circuit breaker answers 503.
        as("CUSTOMER").get().uri("/api/cart").exchange().expectStatus().isEqualTo(503);
        as("CUSTOMER").get().uri("/api/users/me").exchange().expectStatus().isEqualTo(503);
    }

    @Test
    void healthIsPublic() {
        client.get().uri("/actuator/health").exchange().expectStatus().value(s -> {
            if (s == 401 || s == 403) throw new AssertionError("health must be public, got " + s);
        });
    }
}
