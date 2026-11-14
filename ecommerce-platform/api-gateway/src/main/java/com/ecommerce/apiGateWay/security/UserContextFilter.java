package com.ecommerce.apiGateWay.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Propagates the authenticated identity to downstream services.
 *
 * Any X-User-* header sent by the client is removed first, so a caller can never
 * impersonate another user. For authenticated requests the gateway then sets:
 *   X-User-ID    - Keycloak subject ("sub")
 *   X-User-Email - email claim (if present)
 *   X-User-Roles - comma-separated roles, e.g. "CUSTOMER,ADMIN"
 */
@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    public static final String USER_ID = "X-User-ID";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLES = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange cleaned = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.remove(USER_ID);
                    h.remove(USER_EMAIL);
                    h.remove(USER_ROLES);
                }))
                .build();

        return cleaned.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> withIdentity(cleaned, auth))
                .defaultIfEmpty(cleaned)
                .flatMap(chain::filter);
    }

    private ServerWebExchange withIdentity(ServerWebExchange exchange, JwtAuthenticationToken auth) {
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .distinct()
                .collect(Collectors.joining(","));
        String email = auth.getToken().getClaimAsString("email");

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(USER_ID, auth.getToken().getSubject())
                .header(USER_ROLES, roles)
                .headers(h -> {
                    if (email != null) {
                        h.set(USER_EMAIL, email);
                    }
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
