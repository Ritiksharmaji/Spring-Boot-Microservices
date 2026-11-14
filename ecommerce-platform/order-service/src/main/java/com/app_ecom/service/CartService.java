package com.app_ecom.service;

import com.app_ecom.dto.CartItemRequest;
import com.app_ecom.dto.CartItemResponse;
import com.app_ecom.dto.CartResponse;
import com.app_ecom.dto.ProductResponse;
import com.app_ecom.exception.InsufficientStockException;
import com.app_ecom.exception.ResourceNotFoundException;
import com.app_ecom.model.CartItem;
import com.app_ecom.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductGateway productGateway;

    /** Adds a product (or increases its quantity) using the live price and stock from product-service. */
    @Transactional
    public CartResponse addToCart(String userId, CartItemRequest request) {
        ProductResponse product = loadActiveProduct(request.productId());
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, request.productId())
                .orElseGet(() -> newItem(userId, request.productId()));
        int newQuantity = (item.getQuantity() == null ? 0 : item.getQuantity()) + request.quantity();
        applyProduct(item, product, newQuantity);
        cartItemRepository.save(item);
        return getCart(userId);
    }

    /** Sets the quantity of a product already in the cart. */
    @Transactional
    public CartResponse updateQuantity(String userId, Long productId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " is not in your cart"));
        applyProduct(item, loadActiveProduct(productId), quantity);
        return getCart(userId);
    }

    @Transactional
    public void removeItem(String userId, Long productId) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " is not in your cart"));
        cartItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        List<CartItemResponse> items = cartItemRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(i -> new CartItemResponse(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity(),
                        i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        BigDecimal total = items.stream().map(CartItemResponse::subTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = items.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(items, count, total);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserIdOrderByIdAsc(userId);
    }

    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteAllByUserId(userId);
    }

    private ProductResponse loadActiveProduct(Long productId) {
        return productGateway.getProduct(productId)
                .filter(p -> !Boolean.FALSE.equals(p.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found"));
    }

    private void applyProduct(CartItem item, ProductResponse product, int quantity) {
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Only " + product.getStockQuantity()
                    + " unit(s) of '" + product.getName() + "' available");
        }
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        item.setProductName(product.getName());
    }

    private CartItem newItem(String userId, Long productId) {
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        return item;
    }
}
