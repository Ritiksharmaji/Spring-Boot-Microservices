package com.ecommerce.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * E-commerce platform as a modular monolith. Each direct sub-package is an application module
 * (identity, catalog, ordering, payment, notification, shared); Spring Modulith verifies that modules
 * only use each other's public API and delivers domain events between them transactionally.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
