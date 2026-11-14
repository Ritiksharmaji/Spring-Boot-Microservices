Here is your file with **clear, professional, full comments** explaining every part in detail 👇

---

```java
package com.example.demo.NormalClientWithOAuthServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .authorizeHttpRequests(auth -> auth
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
    }
}
```

---

# 🔥 What This Configuration Does Practically

When someone calls your API:

```
GET /data
```

Spring Security does:

1️⃣ Check if Authorization header exists
2️⃣ Check if it contains Bearer token
3️⃣ Validate JWT signature using issuer-uri
4️⃣ Validate expiration
5️⃣ If valid → allow request
6️⃣ If invalid → return 401

---

# 🧠 If You Want Public + Private Endpoints

Example:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**").permitAll()
        .anyRequest().authenticated()
)
```

Now:

* `/public/test` → No token required
* `/data` → Token required

---

If you want, I can now explain:

* 🔐 How JWT validation works internally
* 🏗 How to add role-based access control
* 🚀 How to configure audience validation properly
* 📊 How filter chain works internally

Just tell me what level you want to go — beginner, intermediate, or advanced 😎
