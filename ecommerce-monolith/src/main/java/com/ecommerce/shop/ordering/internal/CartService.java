package com.ecommerce.shop.ordering.internal;

import com.ecommerce.shop.catalog.Catalog;
import com.ecommerce.shop.catalog.ProductInfo;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.ecommerce.shop.ordering.internal.OrderingDtos.*;

@Service
@RequiredArgsConstructor
class CartService {

    private final CartItemRepository cart;
    private final Catalog catalog;

    @Transactional
    public CartResponse add(long userId, AddToCartRequest request) {
        CartItem item = cart.findByUserIdAndProductId(userId, request.productId()).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUserId(userId);
            c.setProductId(request.productId());
            return c;
        });
        int quantity = (item.getQuantity() == null ? 0 : item.getQuantity()) + request.quantity();
        apply(item, product(request.productId()), quantity);
        cart.save(item);
        return view(userId);
    }

    @Transactional
    public CartResponse setQuantity(long userId, long productId, int quantity) {
        CartItem item = cart.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> BusinessException.notFound("Product " + productId + " is not in your cart"));
        apply(item, product(productId), quantity);
        return view(userId);
    }

    @Transactional
    public void remove(long userId, long productId) {
        cart.delete(cart.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> BusinessException.notFound("Product " + productId + " is not in your cart")));
    }

    @Transactional
    public void clear(long userId) {
        cart.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public CartResponse view(long userId) {
        List<CartLine> lines = cart.findByUserIdOrderByIdAsc(userId).stream()
                .map(i -> new CartLine(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new CartResponse(lines,
                lines.stream().mapToInt(CartLine::quantity).sum(),
                lines.stream().map(CartLine::subTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private ProductInfo product(long productId) {
        return catalog.findActiveProduct(productId)
                .orElseThrow(() -> BusinessException.notFound("Product " + productId + " not found"));
    }

    private void apply(CartItem item, ProductInfo product, int quantity) {
        if (product.stockQuantity() < quantity) {
            throw BusinessException.conflict("Insufficient Stock",
                    "Only " + product.stockQuantity() + " unit(s) of '" + product.name() + "' available");
        }
        item.setQuantity(quantity);
        item.setUnitPrice(product.price());
        item.setProductName(product.name());
    }
}
