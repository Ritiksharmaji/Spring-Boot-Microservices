package com.app_ecom.product;

import com.app_ecom.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:products;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ProductApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ProductRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private long createProduct(String name, String price, int stock) throws Exception {
        String body = """
                {"name":"%s","description":"test","price":%s,"stockQuantity":%d,"category":"books"}
                """.formatted(name, price, stock);
        String json = mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void createAndReadProduct_withTypedPriceAndStock() throws Exception {
        long id = createProduct("Clean Code", "499.50", 10);

        mvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(499.50)))
                .andExpect(jsonPath("$.stockQuantity", is(10)))
                .andExpect(jsonPath("$.active", is(true)));

        mvc.perform(get("/api/products").param("category", "BOOKS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));

        mvc.perform(get("/api/products/search").param("keyword", "CLEAN"))
                .andExpect(jsonPath("$.content[0].name", is("Clean Code")));
    }

    @Test
    void invalidProduct_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-5,\"stockQuantity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stockQuantity").exists());
    }

    @Test
    void missingAndDeletedProducts_return404() throws Exception {
        long id = createProduct("Old", "10", 1);
        mvc.perform(delete("/api/products/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/products/{id}", 99999)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Not Found")));
    }

    @Test
    void reserveStock_isAllOrNothing() throws Exception {
        long a = createProduct("A", "10", 5);
        long b = createProduct("B", "20", 1);

        // B is short -> whole reservation fails and A must be untouched (rollback)
        mvc.perform(post("/internal/products/stock/reserve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":%d,\"quantity\":3},{\"productId\":%d,\"quantity\":2}]}"
                                .formatted(a, b)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.productId", is((int) b)));
        assertThat(repository.findById(a).orElseThrow().getStockQuantity()).isEqualTo(5);
        assertThat(repository.findById(b).orElseThrow().getStockQuantity()).isEqualTo(1);

        // enough stock -> both decremented
        mvc.perform(post("/internal/products/stock/reserve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":%d,\"quantity\":3},{\"productId\":%d,\"quantity\":1}]}"
                                .formatted(a, b)))
                .andExpect(status().isNoContent());
        assertThat(repository.findById(a).orElseThrow().getStockQuantity()).isEqualTo(2);
        assertThat(repository.findById(b).orElseThrow().getStockQuantity()).isZero();

        // release puts it back
        mvc.perform(post("/internal/products/stock/release").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":%d,\"quantity\":3}]}".formatted(a)))
                .andExpect(status().isNoContent());
        assertThat(repository.findById(a).orElseThrow().getStockQuantity()).isEqualTo(5);
    }
}
