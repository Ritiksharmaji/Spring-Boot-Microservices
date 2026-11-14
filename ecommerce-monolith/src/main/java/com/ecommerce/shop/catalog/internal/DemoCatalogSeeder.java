package com.ecommerce.shop.catalog.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Demo mode only: fills an empty catalog with sample products (plenty of stock for visitors). */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
class DemoCatalogSeeder implements ApplicationRunner {

    private final ProductRepository products;

    private record Sample(String name, String description, String price, String category, String image) {
    }

    private static final List<Sample> SAMPLES = List.of(
            new Sample("Mechanical Keyboard", "Hot-swappable switches, RGB backlight", "3499.00", "electronics", "keyboard"),
            new Sample("Wireless Mouse", "Silent clicks, 2.4 GHz + Bluetooth", "1299.00", "electronics", "mouse"),
            new Sample("Noise-Cancelling Headphones", "40 h battery, ANC", "7999.00", "electronics", "headphones"),
            new Sample("27\" 4K Monitor", "IPS, USB-C 65 W", "24999.00", "electronics", "monitor"),
            new Sample("Gaming Laptop", "RTX graphics, 32 GB RAM (declined by the demo card limit)", "89999.00", "electronics", "laptop"),
            new Sample("Clean Code", "Robert C. Martin", "549.00", "books", "book1"),
            new Sample("Designing Data-Intensive Applications", "Martin Kleppmann", "1899.00", "books", "book2"),
            new Sample("Building Microservices", "Sam Newman", "1599.00", "books", "book3"),
            new Sample("Cotton T-Shirt", "Organic cotton, unisex", "599.00", "fashion", "tshirt"),
            new Sample("Running Shoes", "Lightweight mesh", "2999.00", "fashion", "shoes"),
            new Sample("Steel Water Bottle", "1 L, keeps cold 24 h", "799.00", "home", "bottle"),
            new Sample("Coffee Mug", "Ceramic, 350 ml", "349.00", "home", "mug"));

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (products.count() > 0) {
            return;
        }
        for (Sample s : SAMPLES) {
            Product p = new Product();
            p.setName(s.name());
            p.setDescription(s.description());
            p.setPrice(new BigDecimal(s.price()));
            p.setStockQuantity(1000);
            p.setCategory(s.category());
            p.setImageUrl("https://picsum.photos/seed/" + s.image() + "/600/400");
            p.setActive(true);
            products.save(p);
        }
        log.info("Demo mode: seeded {} products", SAMPLES.size());
    }
}
