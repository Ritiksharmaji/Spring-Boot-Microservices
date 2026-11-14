package com.app_ecom.product.controller;

import com.app_ecom.product.dto.StockRequest;
import com.app_ecom.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service stock API used by order-service.
 * Lives under /internal, which the API gateway never routes, so clients cannot call it.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/products/stock")
public class InternalStockController {

    private final ProductService productService;

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody StockRequest request) {
        productService.reserveStock(request.items());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@Valid @RequestBody StockRequest request) {
        productService.releaseStock(request.items());
        return ResponseEntity.noContent().build();
    }
}
