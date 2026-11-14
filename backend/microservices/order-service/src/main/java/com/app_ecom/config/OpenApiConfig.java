package com.app_ecom.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI description of this service. "Try it out" calls go through the API gateway
 * with a Keycloak bearer token (the gateway adds the X-User-* identity headers).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceOpenApi(@Value("${app.api.gateway-url:http://localhost:8080}") String gatewayUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0.0")
                        .description("Shopping cart, checkout and order lifecycle (saga with payment-service).")
                        .contact(new Contact().name("E-Commerce Platform").url("https://github.com/Ritiksharmaji/Spring-Boot-Microservices")))
                .servers(List.of(new Server().url(gatewayUrl).description("API gateway")))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Keycloak access token (realm ecom-app)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
