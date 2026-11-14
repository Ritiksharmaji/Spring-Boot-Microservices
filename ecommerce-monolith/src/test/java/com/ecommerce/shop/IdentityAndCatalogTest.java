package com.ecommerce.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdentityAndCatalogTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    TestClient api;
    String admin;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestClient(mvc, json);
        admin = api.login("admin@test.local", "Admin12345");
    }

    private String email() {
        return "u-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    // ---------------- identity ----------------

    @Test
    void registerReturnsCustomerTokenAndRejectsDuplicatesAndWeakPasswords() throws Exception {
        String email = email();
        api.call("POST", "/api/auth/register", null,
                        "{\"firstName\":\"Riya\",\"email\":\"" + email + "\",\"password\":\"Passw0rd!\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")));

        api.call("POST", "/api/auth/register", null,
                        "{\"firstName\":\"Riya\",\"email\":\"" + email.toUpperCase() + "\",\"password\":\"Passw0rd!\"}")
                .andExpect(status().isConflict());

        api.call("POST", "/api/auth/register", null,
                        "{\"firstName\":\"\",\"email\":\"not-an-email\",\"password\":\"short\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginChecksPasswordAndLocksAfterRepeatedFailures() throws Exception {
        String email = email();
        api.register(email);

        api.call("POST", "/api/auth/login", null, "{\"email\":\"" + email + "\",\"password\":\"wrong-pass1\"}")
                .andExpect(status().isUnauthorized());
        for (int i = 0; i < 4; i++) {
            api.call("POST", "/api/auth/login", null, "{\"email\":\"" + email + "\",\"password\":\"wrong-pass1\"}");
        }
        api.call("POST", "/api/auth/login", null, "{\"email\":\"" + email + "\",\"password\":\"Passw0rd!\"}")
                .andExpect(status().isTooManyRequests());

        String other = email();
        api.register(other);
        assertThat(api.login(other, "Passw0rd!")).isNotBlank();
    }

    @Test
    void profileCanBeUpdatedButNotTheRoleOrEmail() throws Exception {
        String token = api.register(email());

        api.call("PUT", "/api/users/me", token, """
                        {"firstName":"Riya","phone":"+919876543210","role":"ADMIN","email":"hacker@x.com",
                         "address":{"street":"MG Road 1","city":"Pune","country":"India","zipcode":"411001"}}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.email", startsWith("u-")))
                .andExpect(jsonPath("$.address.city", is("Pune")));

        api.call("PUT", "/api/users/me", token, "{\"firstName\":\"Riya\",\"phone\":\"12\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void securityRules() throws Exception {
        String customer = api.register(email());

        api.call("GET", "/api/users/me", null, null).andExpect(status().isUnauthorized());
        api.call("GET", "/api/users/me", customer + "x", null).andExpect(status().isUnauthorized()); // tampered
        api.call("GET", "/api/users", customer, null).andExpect(status().isForbidden());
        api.call("GET", "/api/users", admin, null).andExpect(status().isOk());
        api.call("POST", "/api/products", customer, "{}").andExpect(status().isForbidden());
        api.call("POST", "/api/products", null, "{}").andExpect(status().isUnauthorized());
        api.call("GET", "/api/products", null, null).andExpect(status().isOk());
        api.call("GET", "/actuator/health", null, null).andExpect(status().isOk());
        api.call("GET", "/actuator/modulith", customer, null).andExpect(status().isForbidden());
        api.call("GET", "/something-else", customer, null).andExpect(status().isForbidden());
    }

    @Test
    void adminCanPromoteAUser() throws Exception {
        String email = email();
        long id = api.read(api.call("POST", "/api/auth/register", null,
                "{\"firstName\":\"Arjun\",\"email\":\"" + email + "\",\"password\":\"Passw0rd!\"}")).get("user").get("id").asLong();

        api.call("PUT", "/api/users/" + id + "/role", admin, "{\"role\":\"ADMIN\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.role", is("ADMIN")));
        String promoted = api.login(email, "Passw0rd!");
        api.call("GET", "/api/users", promoted, null).andExpect(status().isOk());
    }

    // ---------------- catalog ----------------

    @Test
    void catalogCrudPagingSearchAndSoftDelete() throws Exception {
        long id = api.createProduct(admin, "Clean Code Monolith", "499.50", 10);

        api.call("GET", "/api/products/" + id, null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(499.5)))
                .andExpect(jsonPath("$.stockQuantity", is(10)));
        api.call("GET", "/api/products?category=TEST&size=50", null, null)
                .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)));
        api.call("GET", "/api/products/search?keyword=monolith", null, null)
                .andExpect(jsonPath("$.content[0].name", is("Clean Code Monolith")));

        api.call("POST", "/api/products", admin, "{\"name\":\"\",\"price\":-1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());

        api.call("DELETE", "/api/products/" + id, admin, null).andExpect(status().isNoContent());
        api.call("GET", "/api/products/" + id, null, null).andExpect(status().isNotFound());
    }
}
