package com.app_ecom.product;

import com.app_ecom.product.dto.StockItem;
import com.app_ecom.product.exception.InsufficientStockException;
import com.app_ecom.product.model.Product;
import com.app_ecom.product.repository.ProductRepository;
import com.app_ecom.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against a real PostgreSQL (Testcontainers): many customers try to buy the
 * last units at the same time — the atomic UPDATE must never oversell.
 * Skipped automatically when Docker is not available (runs in CI).
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Testcontainers(disabledWithoutDocker = true)
class ProductStockConcurrencyContainerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired ProductService productService;
    @Autowired ProductRepository repository;

    @Test
    void parallelReservationsNeverOversell() throws Exception {
        Product product = new Product();
        product.setName("Limited edition");
        product.setPrice(new BigDecimal("999.00"));
        product.setStockQuantity(10);
        product.setActive(true);
        long id = repository.save(product).getId();

        List<Callable<Boolean>> buyers = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            buyers.add(() -> {
                try {
                    productService.reserveStock(List.of(new StockItem(id, 1)));
                    return true;
                } catch (InsufficientStockException e) {
                    return false;
                }
            });
        }
        ExecutorService pool = Executors.newFixedThreadPool(8);
        long successful = 0;
        for (Future<Boolean> f : pool.invokeAll(buyers)) {
            if (f.get()) successful++;
        }
        pool.shutdown();

        assertThat(successful).isEqualTo(10);
        assertThat(repository.findById(id).orElseThrow().getStockQuantity()).isZero();
    }
}
