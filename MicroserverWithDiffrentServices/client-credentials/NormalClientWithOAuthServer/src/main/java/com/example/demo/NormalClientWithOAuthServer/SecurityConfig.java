package com.example.demo.NormalClientWithOAuthServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * ---------------------------------------------------------
 * Security Configuration Class
 * ---------------------------------------------------------
 *
 * This class configures Spring Security for the application.
 *
 * It makes this application act as a:
 *
 *   ✅ OAuth2 Resource Server
 *
 * Meaning:
 *   - It expects incoming requests to contain a valid JWT token.
 *   - It validates that token.
 *   - If token is valid → request is allowed.
 *   - If token is invalid or missing → 401 Unauthorized.
 *
 * This is typically used in:
 *   Microservices Architecture
 *   Where services are protected using JWT tokens.
 */
@Configuration
@EnableMethodSecurity  // Enables method-level security like @PreAuthorize
public class SecurityConfig {

    /**
     * ---------------------------------------------------------
     * SecurityFilterChain Bean
     * ---------------------------------------------------------
     *
     * This method defines the main security rules for the application.
     *
     * HttpSecurity is used to configure:
     *   - Authentication
     *   - Authorization
     *   - OAuth2
     *   - CSRF
     *   - Session management
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                /**
                 * ---------------------------------------------------------
                 * CSRF Configuration
                 * ---------------------------------------------------------
                 *
                 * CSRF (Cross Site Request Forgery) protection is enabled
                 * by default in Spring Security.
                 *
                 * For REST APIs using JWT tokens,
                 * CSRF is usually NOT required because:
                 *
                 *   - We are stateless
                 *   - We are not using cookies for authentication
                 *
                 * If needed, you can disable it like:
                 *
                 * .csrf(csrf -> csrf.disable())
                 *
                 */
                // .csrf(csrf -> csrf.disable())


                /**
                 * ---------------------------------------------------------
                 * Authorization Rules
                 * ---------------------------------------------------------
                 *
                 * This defines who can access which endpoints.
                 *
                 * .anyRequest().authenticated()
                 *
                 * Means:
                 *   Every incoming request MUST be authenticated.
                 *
                 * So:
                 *   ✔ Request with valid JWT → allowed
                 *   ❌ No token → 401 Unauthorized
                 *   ❌ Invalid token → 401 Unauthorized
                 */
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().authenticated()
//                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/proxy").permitAll()
                        .anyRequest().authenticated()
                )


                /**
                 * ---------------------------------------------------------
                 * OAuth2 Resource Server Configuration
                 * ---------------------------------------------------------
                 *
                 * This tells Spring:
                 *
                 *   👉 This application is a Resource Server.
                 *
                 * It should:
                 *   - Expect JWT tokens
                 *   - Validate them using issuer-uri
                 *   - Verify signature
                 *   - Check expiration
                 *
                 * Customizer.withDefaults() means:
                 *   Use default JWT validation behavior.
                 */
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                );

        /**
         * Build and return SecurityFilterChain
         */
        return http.build();

        // client config

    }

    // =========== config client code ==========================
    /**
     * --------------------------------------------------------
     * RestTemplate Bean
     * --------------------------------------------------------
     *
     * Used to make HTTP calls to external services.
     * In this example, it is used to call Service-2 API.
     *
     * Note:
     * In modern Spring Boot applications, WebClient is preferred.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * --------------------------------------------------------
     * OAuth2AuthorizedClientService Bean
     * --------------------------------------------------------
     *
     * This service stores OAuth2 authorized client details.
     * That includes:
     *   - Access Token (JWT)
     *   - Refresh Token (if available)
     *
     * We are using InMemory storage.
     *
     * Production Alternative:
     *   JdbcOAuth2AuthorizedClientService (store in DB)
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {

        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * --------------------------------------------------------
     * OAuth2AuthorizedClientManager Bean
     * --------------------------------------------------------
     *
     * This is the MAIN component that:
     *   - Requests access tokens
     *   - Manages token lifecycle
     *   - Handles token refresh
     *
     * We configure it for:
     *   CLIENT CREDENTIALS FLOW
     *
     * Used for:
     *   Machine-to-Machine communication
     *   (No user login involved)
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository repository,
            OAuth2AuthorizedClientService clientService) {

        // Manager responsible for handling OAuth2 clients
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        repository,
                        clientService
                );
        /**
         * Configure which OAuth2 flow to use.
         *
         * Here we are using:
         *   clientCredentials()
         *
         * Meaning:
         *   Service authenticates itself using client-id & client-secret
         */
        OAuth2AuthorizedClientProvider provider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

}
