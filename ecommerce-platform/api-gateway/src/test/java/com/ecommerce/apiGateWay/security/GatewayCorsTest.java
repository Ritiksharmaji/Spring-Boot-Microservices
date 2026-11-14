package com.ecommerce.apiGateWay.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * CORS pre-flight through the real HTTP server (a mock-bound client has no host,
 * which CORS processing needs). Origins come from CORS_ALLOWED_ORIGINS (api-gateway.yml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayCorsTest {

    @Autowired WebTestClient client;

    @MockitoBean ReactiveJwtDecoder jwtDecoder;

    @Test
    void preflightFromAllowedOriginSucceedsWithoutToken() {
        client.options().uri("/api/cart")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
    }

    @Test
    void preflightFromUnknownOriginIsRejected() {
        client.options().uri("/api/cart")
                .header("Origin", "https://evil.example.com")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isForbidden();
    }
}
