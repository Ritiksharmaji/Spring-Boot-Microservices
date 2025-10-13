package com.app_ecom.product.service;

import com.app_ecom.product.dto.ProductRequest;
import com.app_ecom.product.dto.ProductResponse;
import com.app_ecom.product.model.Product;
import com.app_ecom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = new Product();
        updateProductFromRequest(product, productRequest);
        Product saveProduct =  productRepository.save(product);
        return mapToProductResponse(saveProduct);
    }

    private ProductResponse mapToProductResponse(Product saveProduct) {
        ProductResponse response = new ProductResponse();
        response.setId(saveProduct.getId());
        response.setName(saveProduct.getName());
        response.setPrice(String.valueOf(saveProduct.getPrice()));
        response.setActive(saveProduct.getActive());
        response.setImageUrl(saveProduct.getImageUrl());
        response.setCategory(saveProduct.getCategory());
        response.setDescription(saveProduct.getDescription());
        response.setStockQuantity(String.valueOf(saveProduct.getStockQuantity()));

        return response;
    }

    private void updateProductFromRequest(Product product, ProductRequest productRequest) {

        product.setName(productRequest.getName());
        product.setPrice(new BigDecimal(productRequest.getPrice()));
        product.setImageUrl(productRequest.getImageUrl());
        product.setCategory(productRequest.getCategory());
        product.setDescription(productRequest.getDescription());
        product.setStockQuantity(Integer.valueOf(productRequest.getStockQuantity()));

    }


    public Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {
        // first fine the product from the database
        return productRepository.findById(id)
                .map(existingProduct->{
                    updateProductFromRequest(existingProduct, productRequest);
                    Product saveProduct = productRepository.save(existingProduct);
                    return mapToProductResponse(saveProduct);
                });

    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream().map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setActive(false);
                    productRepository.save(product);
                    return true;
                }).orElse(false);
    }

    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::mapToProductResponse)
                .collect((Collectors.toList()));
    }
}
