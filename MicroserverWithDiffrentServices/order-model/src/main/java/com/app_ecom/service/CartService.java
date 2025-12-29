package com.app_ecom.service;

import com.app_ecom.clients.ProductServiceClient;
import com.app_ecom.clients.UserServiceClient;
import com.app_ecom.dto.CartItemRequest;
import com.app_ecom.dto.ProductResponse;
import com.app_ecom.dto.UserResponse;
import com.app_ecom.model.CartItem;
import com.app_ecom.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public boolean addToCart(String userId, CartItemRequest request) {

        // ============================= using loadbalancing ========================
        // first find either product is Exit or not   throw the microservcie
        // looking for product
        ProductResponse productResponse =  productServiceClient.getProductDetails(request.getProductId());
        System.out.println("product found" + productResponse);
        if(productResponse == null){
            return false;
        }
        // verifing the quentity
        if(productResponse.getStockQuantity() < request.getQuantity())
            return false;
        // looking for user
        UserResponse userResponse = userServiceClient.getUserDetails(userId);
        System.out.println("user Found:"+ userResponse);
        if(userResponse.getId() == null){
            return  false;
        }
        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, Long.valueOf(request.getProductId()));
        if (existingCartItem != null){
            // update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
//            existingCartItem.setPrice(existingCartItem.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
            existingCartItem.setPrice(BigDecimal.valueOf(3000.0));
            cartItemRepository.save(existingCartItem);
        }else{
            // create the cartItem
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(Long.valueOf(String.valueOf(Long.valueOf(request.getProductId()))));
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(1000.0));
            cartItemRepository.save(cartItem);
        }
        return true;
    }

    public boolean deleteItemFromCart(String userId, Long productId) {
       CartItem cartItem =  cartItemRepository.findByUserIdAndProductId(userId, productId);
        if(cartItem != null){
            cartItemRepository.delete(cartItem);
            return true;
        }

        return false;
    }

    public List<CartItem> getCart(String userId) {

        return cartItemRepository.findByUserId(userId);
    }


    public void clearCart(String userId) {
       cartItemRepository.deleteByUserId(userId);
    }

    public boolean productFallback(String userId, CartItemRequest request, Exception ex) {
        System.out.println("fallback called");
        return false;
    }
}
