package com.app_ecom.service;

import com.app_ecom.dto.CartItemRequest;
import com.app_ecom.model.CartItem;
import com.app_ecom.repository.CartItemRepository;
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

    public boolean addToCart(Long userId, CartItemRequest request) {

        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, Long.valueOf(request.getProductId()));

        if (existingCartItem != null){
            // update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
//            existingCartItem.setPrice(existingCartItem.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
            existingCartItem.setPrice(BigDecimal.valueOf(1000.0));
            cartItemRepository.save(existingCartItem);
        }else{
            // create the cartItem
            CartItem cartItem = new CartItem();
            cartItem.setUserId(Long.valueOf(userId));
            cartItem.setProductId(Long.valueOf(String.valueOf(Long.valueOf(request.getProductId()))));
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(1000.0));
            cartItemRepository.save(cartItem);
        }
        return true;
    }

    public boolean deleteItemFromCart(Long userId, Long productId) {

       CartItem cartItem =  cartItemRepository.findByUserIdAndProductId(userId, Long.valueOf(String.valueOf(productId)));
        if(cartItem != null){
            cartItemRepository.delete(cartItem);
            return true;
        }

        return false;
    }

    public List<CartItem> getCart(String userId) {

        return cartItemRepository.findByUserId(Long.valueOf(userId));
    }


    public void clearCart(String userId) {
       cartItemRepository.deleteByUserId(Long.valueOf(userId));
    }
}
