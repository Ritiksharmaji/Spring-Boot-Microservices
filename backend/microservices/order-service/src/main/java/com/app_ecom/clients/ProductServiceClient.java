package com.app_ecom.clients;

import com.app_ecom.dto.ProductResponse;
import com.app_ecom.dto.StockRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Declarative HTTP client for product-service (resolved through Eureka by the load-balanced RestClient). */
@HttpExchange
public interface ProductServiceClient {

    @GetExchange("/api/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);

    @PostExchange("/internal/products/stock/reserve")
    void reserveStock(@RequestBody StockRequest request);

    @PostExchange("/internal/products/stock/release")
    void releaseStock(@RequestBody StockRequest request);
}
