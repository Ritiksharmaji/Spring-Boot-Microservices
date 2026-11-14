package com.ecommerce.apiGateWay.security;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextFilterTest {

    private final UserContextFilter filter = new UserContextFilter();

    @Test
    void spoofedIdentityHeadersAreRemovedForAnonymousRequests() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/products")
                .header("X-User-ID", "someone-else")
                .header("X-User-Roles", "ADMIN"));

        ServerWebExchange forwarded = run(exchange);

        assertThat(forwarded.getRequest().getHeaders().containsKey("X-User-ID")).isFalse();
        assertThat(forwarded.getRequest().getHeaders().containsKey("X-User-Roles")).isFalse();
    }

    @Test
    void identityComesFromTheVerifiedTokenNotTheClient() {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none")
                .subject("kc-alice").claim("email", "alice@example.com").build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"), new SimpleGrantedAuthority("SCOPE_profile")));
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/cart")
                        .header("X-User-ID", "someone-else"))
                .mutate().principal(Mono.just(auth)).build();

        ServerWebExchange forwarded = run(exchange);

        assertThat(forwarded.getRequest().getHeaders().get("X-User-ID")).containsExactly("kc-alice");
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo("CUSTOMER");
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-User-Email")).isEqualTo("alice@example.com");
    }

    private ServerWebExchange run(ServerWebExchange exchange) {
        AtomicReference<ServerWebExchange> seen = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            seen.set(ex);
            return Mono.empty();
        };
        filter.filter(exchange, chain).block();
        return seen.get();
    }
}
