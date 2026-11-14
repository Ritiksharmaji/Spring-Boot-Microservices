package com.ecommerce.shop.ordering;


import java.math.BigDecimal;
import java.util.List;

/** Published when a customer checks out (order PENDING, stock already reserved). */
public record OrderPlaced(long orderId, long userId, String customerEmail, String customerName,
                          BigDecimal totalAmount, List<Line> lines) {

    public record Line(long productId, String productName, int quantity, BigDecimal unitPrice) {
    }
}
