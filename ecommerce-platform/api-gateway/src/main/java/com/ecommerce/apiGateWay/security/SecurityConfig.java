package com.ecommerce.apiGateWay.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity){
//        return
//                httpSecurity
//                        .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                        .authorizeExchange(exchange-> exchange.anyExchange().authenticated())
//                        .oauth2ResourceServer(oauth2-> oauth2.jwt(Customizer.withDefaults()))
//                .build();
//    }
//}

// =================== role based configuration  ====================

@Configuration
@EnableWebFluxSecurity   // Enables Spring Security for WebFlux (Reactive)
public class SecurityConfig {

    /**
     * Main Security Filter Chain for API Gateway
     *
     * - Disables CSRF (because we are stateless JWT-based API)
     * - Requires authentication for all requests
     * - Configures OAuth2 Resource Server with JWT validation
     * - Uses custom JWT converter to extract roles from Keycloak
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {

        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Stateless API → disable CSRF

//                .authorizeExchange(exchange ->
//                        exchange
//                                .anyExchange().authenticated()  // All routes require authentication
//                )
                .authorizeExchange(exchange ->
                        exchange
                                .pathMatchers("/api/products/**").hasRole("PRODUCT")
                                .pathMatchers("/api/orders/**").hasRole("ORDER")
                                .pathMatchers("/api/cart/**").hasRole("ORDER")
                                .pathMatchers("/api/users/**").hasRole("USER")
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(grantedAuthorities())
                        )
                )
                .build();
    }

    /**
     * Custom Converter to extract roles from Keycloak JWT
     *
     * Keycloak stores client roles inside:
     *
     * "resource_access": {
     *    "oauth2-ecom-pkce": {
     *        "roles": ["ADMIN", "USER"]
     *    }
     * }
     *
     * We convert them into Spring Security authorities:
     *
     * ROLE_ADMIN
     * ROLE_USER
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthorities() {

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter =
                new ReactiveJwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // ===== 1. Realm Roles =====
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> realmRoles =
                        (List<String>) realmAccess.get("roles");

                realmRoles.forEach(role ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }

            // ===== 2. Client Roles =====
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess != null) {

                Map<String, Object> client =
                        (Map<String, Object>) resourceAccess.get("oauth2-ecom-pkce");

                if (client != null && client.containsKey("roles")) {

                    List<String> clientRoles =
                            (List<String>) client.get("roles");

                    clientRoles.forEach(role ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                }
            }

            System.out.println("Authorities: " + authorities);

            return Flux.fromIterable(authorities);
        });

        return jwtAuthenticationConverter;
    }

}

