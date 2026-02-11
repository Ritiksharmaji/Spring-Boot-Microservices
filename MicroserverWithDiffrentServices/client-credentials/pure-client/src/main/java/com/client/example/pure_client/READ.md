Perfect 👌 Ritik — this is a **complete Client Credentials Flow example** calling another microservice using Keycloak.

I’ll now rewrite your entire file with:

* ✅ Clean formatting
* ✅ Proper naming
* ✅ Enterprise-level comments
* ✅ Clear flow explanation

---

# ✅ Fully Commented Version

```java
package com.client.example.pure_client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PureClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(PureClientApplication.class, args);
    }

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


    /**
     * --------------------------------------------------------
     * CommandLineRunner Bean
     * --------------------------------------------------------
     *
     * This runs automatically when application starts.
     *
     * Flow:
     *
     * 1️⃣ Request Access Token from Keycloak
     * 2️⃣ Extract JWT
     * 3️⃣ Add JWT to Authorization header
     * 4️⃣ Call Service-2 protected endpoint
     */
    @Bean
    public CommandLineRunner run(
            OAuth2AuthorizedClientManager manager,
            RestTemplate restTemplate,
            @Value("${service2.url}") String service2Url) {

        return args -> {

            /**
             * Step 1: Create authorization request
             *
             * clientRegistrationId:
             *   Must match the registration ID defined in application.yml
             *
             * principal:
             *   Represents who is requesting token.
             *   Since this is machine-to-machine → we use dummy value.
             */
            OAuth2AuthorizeRequest authorizeRequest =
                    OAuth2AuthorizeRequest
                            .withClientRegistrationId("keycloak-client")
                            .principal("machine")
                            .build();


            /**
             * Step 2: Request Access Token
             */
            OAuth2AuthorizedClient authorizedClient =
                    manager.authorize(authorizeRequest);

            if (authorizedClient == null) {
                throw new RuntimeException("Failed to authorize client");
            }

            // Extract JWT access token
            String accessToken =
                    authorizedClient.getAccessToken().getTokenValue();


            /**
             * Step 3: Add token to Authorization header
             *
             * Header:
             *   Authorization: Bearer <token>
             */
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);


            /**
             * Step 4: Call Protected API (Service-2)
             */
            var response = restTemplate.exchange(
                    service2Url + "/data",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );


            /**
             * Step 5: Print response
             */
            System.out.println("Response from Service-2: " + response.getBody());
        };
    }

}
```

---

# 🔥 What Happens When You Start This App?

### Startup Flow

```
PureClientApplication starts
        ↓
CommandLineRunner executes
        ↓
Requests token from Keycloak
        ↓
Gets JWT
        ↓
Calls Service-2 /data endpoint
        ↓
Service-2 validates token
        ↓
Returns response
```

---

# 🧠 Architecture Behind This

```
PureClient (This App)
        │
        │  (1) Client Credentials Flow
        ▼
Keycloak
        │
        │  (2) Returns JWT
        ▼
PureClient
        │
        │  (3) Calls Service-2 with Bearer Token
        ▼
Service-2 (Resource Server)
        │
        │  (4) Validates JWT
        ▼
Response
```

---

# 🚀 Production Improvements (Very Important)

For real microservice architecture:

### Instead of:

```
RestTemplate
```

Use:

```
WebClient + OAuth2 Filter
```

Because:

* Non-blocking
* Better performance
* Native Spring Security integration

---

# ⚠️ Important Notes

1️⃣ `"keycloak-client"` must match:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak-client:
```

2️⃣ `service2.url` must be defined:

```yaml
service2:
  url: http://localhost:8082
```

3️⃣ Service-2 must be configured as:

```java
.oauth2ResourceServer().jwt()
```

---

If you want next, I can give you:

* 🔥 Full Keycloak setup with realm + client config
* 🔥 Service-2 Resource Server code
* 🔥 API Gateway + Keycloak integration
* 🔥 WebClient version (modern production way)
* 🔥 Complete Docker Compose setup

Just tell me what you want next 👌
