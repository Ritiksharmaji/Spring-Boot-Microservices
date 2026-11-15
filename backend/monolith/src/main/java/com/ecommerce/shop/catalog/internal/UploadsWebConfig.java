package com.ecommerce.shop.catalog.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/** Serves uploaded product images. File names are random and never reused, so they cache for a year. */
@Configuration
@RequiredArgsConstructor
class UploadsWebConfig implements WebMvcConfigurer {

    private final ProductImageStorage storage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ProductImageStorage.URL_PREFIX + "**")
                .addResourceLocations(storage.directory().toUri().toString())
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
    }
}
