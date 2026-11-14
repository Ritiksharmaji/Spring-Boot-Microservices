package com.app_ecom.product.service;

import com.app_ecom.product.dto.ProductRequest;
import com.app_ecom.product.dto.ProductResponse;
import com.app_ecom.product.dto.StockItem;
import com.app_ecom.product.exception.InsufficientStockException;
import com.app_ecom.product.exception.ResourceNotFoundException;
import com.app_ecom.product.model.Product;
import com.app_ecom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        if (request.getActive() == null) {
            product.setActive(true);
        }
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> notFound(id));
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String category, Pageable pageable) {
        Page<Product> page = StringUtils.hasText(category)
                ? productRepository.findByActiveTrueAndCategoryIgnoreCase(category, pageable)
                : productRepository.findByActiveTrue(pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Pageable pageable) {
        return productRepository.search(keyword.trim(), pageable).map(this::toResponse);
    }

    /** Soft delete: the product disappears from the catalog but old orders keep their reference. */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> notFound(id));
        product.setActive(false);
    }

    /**
     * Reserves stock for every line of an order, all-or-nothing:
     * if any line is short, the exception rolls back the decrements already made.
     */
    @Transactional
    public void reserveStock(List<StockItem> items) {
        for (StockItem item : items) {
            int updated = productRepository.decreaseStock(item.productId(), item.quantity());
            if (updated == 0) {
                throw new InsufficientStockException(item.productId(), item.quantity());
            }
        }
        log.info("Reserved stock for {} product line(s)", items.size());
    }

    /** Returns stock for a cancelled / failed order. */
    @Transactional
    public void releaseStock(List<StockItem> items) {
        for (StockItem item : items) {
            if (productRepository.increaseStock(item.productId(), item.quantity()) == 0) {
                log.warn("Release skipped: product {} no longer exists", item.productId());
            }
        }
        log.info("Released stock for {} product line(s)", items.size());
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategory(product.getCategory());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreateAt());
        response.setUpdatedAt(product.getUpdateAt());
        return response;
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Product " + id + " not found");
    }
}
