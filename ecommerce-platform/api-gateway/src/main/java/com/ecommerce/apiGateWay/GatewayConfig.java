package com.ecommerce.apiGateWay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Routes (resolved through Eureka with lb://) with a circuit breaker per downstream service.
 * The product catalog, the busiest public endpoint, also gets Redis rate limiting.
 * No retries at the edge: services retry their own downstream reads, and retrying here would
 * multiply load during an outage (and cannot retry once the fallback response is committed).
 * /internal/** endpoints of the services are intentionally not routed.
 */
@Configuration
public class GatewayConfig {

    @Value("${app.eureka-dashboard-url:http://localhost:8761}")
    private String eurekaDashboardUrl;

    /** 10 requests/second per client, bursts up to 20. */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /** Rate-limit key: client IP (raw address, no blocking reverse-DNS lookup). */
    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> Mono.just(Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(a -> a.getHostAddress())
                .orElse("unknown"));
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("userBreaker")
                                .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-service"))
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(clientIpKeyResolver()))
                                .circuitBreaker(c -> c
                                        .setName("productBreaker")
                                        .setFallbackUri("forward:/fallback/products")))
                        .uri("lb://product-service"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("orderBreaker")
                                .setFallbackUri("forward:/fallback/orders")))
                        .uri("lb://order-service"))
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("cartBreaker")
                                .setFallbackUri("forward:/fallback/cart")))
                        .uri("lb://order-service"))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("paymentBreaker")
                                .setFallbackUri("forward:/fallback/payments")))
                        .uri("lb://payment-service"))
                // OpenAPI documents of each service, aggregated by the gateway's Swagger UI
                .route("docs-user-service", r -> r.path("/docs/user-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs")).uri("lb://user-service"))
                .route("docs-product-service", r -> r.path("/docs/product-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs")).uri("lb://product-service"))
                .route("docs-order-service", r -> r.path("/docs/order-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs")).uri("lb://order-service"))
                .route("docs-payment-service", r -> r.path("/docs/payment-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs")).uri("lb://payment-service"))
                // Eureka dashboard (ADMIN only, see SecurityConfig)
                .route("eureka-server", r -> r
                        .path("/eureka/main")
                        .filters(f -> f.setPath("/"))
                        .uri(eurekaDashboardUrl))
                .route("eureka-server-static", r -> r
                        .path("/eureka/**")
                        .uri(eurekaDashboardUrl))
                .build();
    }
}
