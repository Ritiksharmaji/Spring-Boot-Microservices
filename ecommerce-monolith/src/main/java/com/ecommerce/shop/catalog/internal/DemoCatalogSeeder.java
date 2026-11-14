package com.ecommerce.shop.catalog.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demo mode only: loads the clothing catalog used by the React storefront (52 products,
 * images served by the frontend under /products/...). Runs when the catalog has no
 * clothing yet; any older demo products are soft-deleted so the shop shows one catalog.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
class DemoCatalogSeeder implements ApplicationRunner {

    private static final List<String> CLOTHING = List.of("Men", "Women", "Kids");

    private final ProductRepository products;
    private final ObjectMapper json;

    record DemoProduct(String name, String description, BigDecimal price, String category, String subCategory,
                       List<String> sizes, boolean bestseller, List<String> images) {
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (products.existsByActiveTrueAndCategoryIn(CLOTHING)) {
            return;
        }
        int retired = products.retireAllActive();
        List<DemoProduct> demo;
        try (InputStream in = new ClassPathResource("demo/catalog.json").getInputStream()) {
            demo = json.readValue(in, new TypeReference<>() { });
        }
        for (DemoProduct d : demo) {
            Product p = new Product();
            p.setName(d.name());
            p.setDescription(d.description());
            p.setPrice(d.price().multiply(BigDecimal.TEN));   // demo prices in rupees
            p.setStockQuantity(500);
            p.setCategory(d.category());
            p.setSubCategory(d.subCategory());
            p.setSizes(d.sizes());
            p.setBestseller(d.bestseller());
            p.getImages().addAll(d.images());
            p.setImageUrl(d.images().isEmpty() ? null : d.images().getFirst());
            p.setActive(true);
            products.save(p);
        }
        log.info("Demo mode: loaded {} clothing products (retired {} older demo products)", demo.size(), retired);
    }
}
