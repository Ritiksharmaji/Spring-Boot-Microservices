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

/** Cart lines are per product AND size; stock is checked across all sizes of a product. */
@Service
@RequiredArgsConstructor
class CartService {

    private final CartItemRepository cart;
    private final Catalog catalog;

    @Transactional
    public CartResponse add(long userId, AddToCartRequest request) {
        ProductInfo product = product(request.productId());
        String size = normalize(request.size());
        checkSize(product, size);
        CartItem item = cart.findByUserIdAndProductIdAndSize(userId, request.productId(), size).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUserId(userId);
            c.setProductId(request.productId());
            c.setSize(size);
            c.setQuantity(0);
            return c;
        });
        apply(userId, item, product, item.getQuantity() + request.quantity());
        cart.save(item);
        return view(userId);
    }

    @Transactional
    public CartResponse setQuantity(long userId, long productId, String size, int quantity) {
        CartItem item = line(userId, productId, size);
        apply(userId, item, product(productId), quantity);
        return view(userId);
    }

    @Transactional
    public void remove(long userId, long productId, String size) {
        cart.delete(line(userId, productId, size));
    }

    @Transactional
    public void clear(long userId) {
        cart.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public CartResponse view(long userId) {
        List<CartLine> lines = cart.findByUserIdOrderByIdAsc(userId).stream()
                .map(i -> new CartLine(i.getProductId(), i.getProductName(), i.getSize(),
                        catalog.findActiveProduct(i.getProductId()).map(ProductInfo::imageUrl).orElse(null),
                        i.getUnitPrice(), i.getQuantity(), i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new CartResponse(lines,
                lines.stream().mapToInt(CartLine::quantity).sum(),
                lines.stream().map(CartLine::subTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private CartItem line(long userId, long productId, String size) {
        return cart.findByUserIdAndProductIdAndSize(userId, productId, normalize(size))
                .orElseThrow(() -> BusinessException.notFound("Product " + productId + " is not in your cart"));
    }

    private ProductInfo product(long productId) {
        return catalog.findActiveProduct(productId)
                .orElseThrow(() -> BusinessException.notFound("Product " + productId + " not found"));
    }

    private void checkSize(ProductInfo product, String size) {
        if (!product.acceptsSize(size)) {
            throw new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid Size",
                    product.sizes().isEmpty() ? "'" + product.name() + "' has no sizes"
                            : "Choose a size for '" + product.name() + "': " + String.join(", ", product.sizes()));
        }
    }

    /** Stock check counts every size line of the same product. */
    private void apply(long userId, CartItem item, ProductInfo product, int quantity) {
        int otherSizes = cart.findByUserIdOrderByIdAsc(userId).stream()
                .filter(c -> c.getProductId().equals(product.id()) && !c.getSize().equals(item.getSize()))
                .mapToInt(CartItem::getQuantity).sum();
        if (product.stockQuantity() < quantity + otherSizes) {
            throw BusinessException.conflict("Insufficient Stock",
                    "Only " + product.stockQuantity() + " unit(s) of '" + product.name() + "' available");
        }
        item.setQuantity(quantity);
        item.setUnitPrice(product.price());
        item.setProductName(product.name());
    }

    static String normalize(String size) {
        return size == null ? "" : size.trim().toUpperCase();
    }
}
