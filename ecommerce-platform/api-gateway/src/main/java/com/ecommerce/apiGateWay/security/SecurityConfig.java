package com.ecommerce.apiGateWay.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Gateway security (Keycloak / OAuth2 resource server).
 *
 * Roles (Keycloak realm roles or client roles of "oauth2-ecom-pkce"):
 *   CUSTOMER - shop: cart, orders, own profile
 *   ADMIN    - manage catalog, all users, order status
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String CLIENT_ID = "oauth2-ecom-pkce";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // stateless JWT API
                .authorizeExchange(ex -> ex
                        // public
                        .pathMatchers("/actuator/health/**", "/actuator/info", "/fallback/**").permitAll()
                        // API documentation (switch off in production with API_DOCS_ENABLED=false)
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**",
                                "/docs/*/v3/api-docs").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        // admin
                        .pathMatchers("/api/products/**", "/api/products").hasRole("ADMIN")
                        .pathMatchers("/api/orders/admin", "/api/orders/*/status", "/api/payments/admin").hasRole("ADMIN")
                        .pathMatchers("/eureka/**", "/actuator/**").hasRole("ADMIN")
                        // signed-in customers (own profile, cart, orders)
                        .pathMatchers("/api/users/me").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                        .pathMatchers("/api/cart/**", "/api/cart", "/api/orders/**", "/api/orders", "/api/payments/**")
                            .hasAnyRole("CUSTOMER", "ADMIN")
                        // everything else is closed
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter())))
                .build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Flux.fromIterable(extractRoles(jwt)));
        return converter;
    }

    /** Collects Keycloak realm roles and client roles as ROLE_* authorities. */
    @SuppressWarnings("unchecked")
    static Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            ((List<String>) roles).forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.get(CLIENT_ID) instanceof Map<?, ?> client
                && client.get("roles") instanceof List<?> roles) {
            ((List<String>) roles).forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        return authorities;
    }
}
