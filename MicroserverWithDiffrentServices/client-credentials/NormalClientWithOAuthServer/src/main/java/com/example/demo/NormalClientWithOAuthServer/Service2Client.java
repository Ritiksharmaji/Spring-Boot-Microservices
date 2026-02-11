package com.example.demo.NormalClientWithOAuthServer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/*
is is used to fetch the data from the resource server using fetch method
 */
@Service
public class Service2Client {

    private final RestTemplate rest;
    private final OAuth2AuthorizedClientManager manager;

    @Value("${service2.url}")
    String service2Url;

    public Service2Client(RestTemplate rest, OAuth2AuthorizedClientManager manager) {
        this.rest = rest;
        this.manager = manager;
    }

//    public String fetchData(){
//        /**
//         * Step 1: Create authorization request
//         *
//         * clientRegistrationId:
//         *   Must match the registration ID defined in application.yml
//         *
//         * principal:
//         *   Represents who is requesting token.
//         *   Since this is machine-to-machine → we use dummy value.
//         */
//        OAuth2AuthorizeRequest authorizeRequest =
//                OAuth2AuthorizeRequest
//                        .withClientRegistrationId("keycloak-client")
//                        .principal("machine")
//                        .build();
//
//
//        /**
//         * Step 2: Request Access Token
//         */
//        OAuth2AuthorizedClient authorizedClient =
//                manager.authorize(authorizeRequest);
//
//        if (authorizedClient == null) {
//            throw new RuntimeException("Failed to authorize client");
//        }
//
//        // Extract JWT access token
//        String accessToken =
//                authorizedClient.getAccessToken().getTokenValue();
//
//
//        /**
//         * Step 3: Add token to Authorization header
//         *
//         * Header:
//         *   Authorization: Bearer <token>
//         */
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//
//
//        /**
//         * Step 4: Call Protected API (Service-2)
//         */
//        var response = rest.exchange(
//                service2Url + "/data",
//                HttpMethod.GET,
//                new HttpEntity<>(headers),
//                String.class
//        );
//
//        /**
//         * Step 5: Print response
//         */
//        //System.out.println("Response from Service-2: " + response.getBody());
//        return response.getBody();
//    }

    // ========= fetch method for same use token from postman ============

    public String fetchData() {

        // 1️⃣ Get current authentication
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("No JWT token found");
        }

        // 2️⃣ Extract token value
        String accessToken = jwtAuth.getToken().getTokenValue();

        // 3️⃣ Add token to header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // 4️⃣ Call Service-2 with SAME token
        var response = rest.exchange(
                service2Url + "/data",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        return response.getBody();
    }

}
