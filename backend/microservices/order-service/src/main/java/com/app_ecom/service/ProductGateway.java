package com.app_ecom.service;

import com.app_ecom.clients.ProductServiceClient;
import com.app_ecom.dto.ProductResponse;
import com.app_ecom.dto.StockRequest;
import com.app_ecom.exception.BusinessException;
import com.app_ecom.exception.InsufficientStockException;
import com.app_ecom.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

/**
 * Resilient access to product-service.
 * Reads are retried; stock reservation is NOT retried because it is not idempotent.
 * 4xx answers become business exceptions (ignored by the circuit breaker);
 * network errors / 5xx trip the breaker and surface as 503.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductGateway {

    private static final String CB = "productService";

    private final ProductServiceClient client;

    @Retry(name = CB)
    @CircuitBreaker(name = CB, fallbackMethod = "getProductFallback")
    public Optional<ProductResponse> getProduct(Long productId) {
        try {
            return Optional.ofNullable(client.getProduct(productId));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @CircuitBreaker(name = CB, fallbackMethod = "stockFallback")
    public void reserveStock(StockRequest request) {
        try {
            client.reserveStock(request);
        } catch (HttpClientErrorException.Conflict e) {
            throw new InsufficientStockException(
                    "Not enough stock to place this order. Please review the quantities in your cart.");
        }
    }

    @Retry(name = CB)
    @CircuitBreaker(name = CB, fallbackMethod = "stockFallback")
    public void releaseStock(StockRequest request) {
        client.releaseStock(request);
    }

    @SuppressWarnings("unused")
    public Optional<ProductResponse> getProductFallback(Long productId, Throwable t) {
        throw translate(t);
    }

    @SuppressWarnings("unused")
    public void stockFallback(StockRequest request, Throwable t) {
        throw translate(t);
    }

    private RuntimeException translate(Throwable t) {
        if (t instanceof BusinessException be) {
            return be;
        }
        return new ServiceUnavailableException("Product service is unavailable, please try again later", t);
    }
}
