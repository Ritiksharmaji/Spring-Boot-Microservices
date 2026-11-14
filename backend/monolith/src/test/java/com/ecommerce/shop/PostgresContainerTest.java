package com.ecommerce.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Real PostgreSQL (Testcontainers; skipped without Docker, runs in CI): Flyway migrations,
 * Hibernate schema validation, and 20 customers racing for 5 units — never oversold.
 */
@SpringBootTest(properties = {"app.bootstrap.admin-email=admin@pg.local", "app.bootstrap.admin-password=Admin12345"})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PostgresContainerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void concurrentCheckoutsNeverOversellAndSagaCompletes() throws Exception {
        TestClient api = new TestClient(mvc, json);
        String admin = api.login("admin@pg.local", "Admin12345");
        long product = api.createProduct(admin, "Limited", "999.00", 5);

        List<String> buyers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String token = api.register("buyer" + i + "@pg.local");
            api.saveAddress(token);
            api.call("POST", "/api/cart/items", token, "{\"productId\":" + product + ",\"quantity\":1}");
            buyers.add(token);
        }

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<Integer>> checkouts = buyers.stream()
                .<Callable<Integer>>map(t -> () -> api.call("POST", "/api/orders", t, null).andReturn().getResponse().getStatus())
                .toList();
        long created = 0;
        for (Future<Integer> f : pool.invokeAll(checkouts)) {
            if (f.get() == 201) created++;
        }
        pool.shutdown();

        assertThat(created).isEqualTo(5);
        assertThat(api.read(api.call("GET", "/api/products/" + product, null, null)).get("stockQuantity").asInt()).isZero();
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(api.read(api.call("GET", "/api/orders/admin?size=50", admin, null)).get("content").findValuesAsText("status"))
                        .hasSize(5).containsOnly("CONFIRMED"));
    }
}
