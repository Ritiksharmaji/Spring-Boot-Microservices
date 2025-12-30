package com.ecommerce.apiGateWay;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    // by normal
//    @Bean
//    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
//
//        return builder.routes()
//
//                // USER SERVICE (Direct URL)
//                .route("user-service", r -> r
//                        .path("/api/users/**")
//                        .uri("lb://USER-SERVICE"))
//
//                // PRODUCT SERVICE (Load Balanced via Eureka)
//                .route("product-service", r -> r
//                        .path("/api/products/**")
//                        .filters(f -> f.circuitBreaker(config -> config
//                                .setName("ecomBreaker")
//                                .setFallbackUri("forward:/fallback/products")
//                        ))
//                        .uri("lb://PRODUCT-SERVICE"))
//
//                // ORDER + CART SERVICE
//                .route("order-service", r -> r
//                        .path("/api/orders/**", "/api/cart/**")
//                        .uri("lb://ORDER-SERVICE"))
//
//                // EUREKA UI MAIN PAGE
//                .route("eureka-server", r -> r
//                        .path("/eureka/main")
//                        .filters(f -> f.setPath("/"))
//                        .uri("http://localhost:8761"))
//
//                // EUREKA STATIC CONTENT
//                .route("eureka-server-static", r -> r
//                        .path("/eureka/**")
//                        .uri("http://localhost:8761"))
//
//                .build();
//    }

    // --------------- by Rewrite filter --------
//    @Bean
//    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
//
//        return builder.routes()
//
//                // USER SERVICE
//                .route("user-service", r -> r
//                        .path("/users/**")
//                        .filters(f -> f
//                                .rewritePath("/users(?<segment>/?.*)", "/api/users${segment}")
//                        )
//                        .uri("lb://USER-SERVICE")
//                )
//
//
//                // PRODUCT SERVICE ✅ FIXED
//                .route("product-service", r -> r
//                        .path("/products/**")
//                        .filters(f -> f
//                                .rewritePath("/products(?<segment>/?.*)", "/api/products${segment}")
//                        )
//                        .uri("lb://PRODUCT-SERVICE"))
//
//                // ORDER + CART SERVICE
//                .route("order-service", r -> r
//                        .path("/orders/**", "/cart/**")
//                        .filters(f -> f
//                                .rewritePath("/orders/(?<segment>.*)", "/api/orders/${segment}")
//                                .rewritePath("/cart/(?<segment>.*)", "/api/cart/${segment}")
//                        )
//                        .uri("lb://ORDER-SERVICE"))
//
//                // EUREKA MAIN PAGE
//                .route("eureka-server", r -> r
//                        .path("/eureka/main")
//                        .filters(f -> f.setPath("/"))
//                        .uri("http://localhost:8761"))
//
//                // EUREKA STATIC CONTENT
//                .route("eureka-server-static", r -> r
//                        .path("/eureka/**")
//                        .uri("http://localhost:8761"))
//
//                .build();
//    }

    // ========= by  circulte breacker ====

    // for redis

    @Bean
    public RedisRateLimiter redisRateLimiter(){
        return new RedisRateLimiter(10,20,1);
    }

//    @Bean
//    public KeyResolver userKeyResolver() {
//        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("user"));
//    }

    @Bean
    public KeyResolver HostNameKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().
                getRemoteAddress().getHostName());
    }



    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                // USER SERVICE
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("userBreaker")
                                .setFallbackUri("forward:/fallback/users")
                        ))
                        .uri("lb://USER-SERVICE"))

                // PRODUCT SERVICE
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .retry(retryConfig -> retryConfig
                                        .setRetries(10)
                                        .setMethods(HttpMethod.GET)
                                )
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(HostNameKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("productBreaker")
                                        .setFallbackUri("forward:/fallback/products")
                                )
                        )
                        .uri("lb://PRODUCT-SERVICE")
                )

                // ORDER SERVICE
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("orderBreaker")
                                .setFallbackUri("forward:/fallback/orders")
                        ))
                        .uri("lb://ORDER-SERVICE"))

                // CART SERVICE
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("cartBreaker")
                                .setFallbackUri("forward:/fallback/cart")
                        ))
                        .uri("lb://ORDER-SERVICE"))

                // EUREKA MAIN PAGE
                .route("eureka-server", r -> r
                        .path("/eureka/main")
                        .filters(f -> f.setPath("/"))
                        .uri("http://localhost:8761"))

                // EUREKA STATIC CONTENT
                .route("eureka-server-static", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8761"))

                .build();
    }

}
