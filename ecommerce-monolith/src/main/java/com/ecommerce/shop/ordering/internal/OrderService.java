package com.ecommerce.shop.ordering.internal;

import com.ecommerce.shop.catalog.Catalog;
import com.ecommerce.shop.catalog.ProductInfo;
import com.ecommerce.shop.catalog.StockLine;
import com.ecommerce.shop.identity.Customer;
import com.ecommerce.shop.identity.CustomerDirectory;
import com.ecommerce.shop.ordering.OrderPayments;
import com.ecommerce.shop.ordering.OrderPlaced;
import com.ecommerce.shop.ordering.OrderStatus;
import com.ecommerce.shop.ordering.OrderStatusChanged;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.ecommerce.shop.ordering.internal.OrderingDtos.OrderResponse;

/**
 * Checkout and order lifecycle. Unlike the microservices version, stock reservation, the order,
 * clearing the cart and the OrderPlaced event all commit in ONE local transaction — no compensation
 * is needed if saving fails. Payment happens asynchronously after commit (payment module).
 */
@Slf4j
@Service
@RequiredArgsConstructor
class OrderService implements OrderPayments {

    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final Catalog catalog;
    private final CustomerDirectory customers;
    private final ApplicationEventPublisher events;

    @Transactional
    public OrderResponse checkout(long userId) {
        List<CartItem> items = cart.findByUserIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            throw BusinessException.invalid("Empty Cart", "Your cart is empty");
        }
        Customer customer = customers.findCustomer(userId)
                .filter(c -> c.address() != null)
                .orElseThrow(() -> BusinessException.invalid("Profile Incomplete",
                        "Add your delivery address (PUT /api/users/me) before placing an order"));

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setStatusReason("Awaiting payment");
        order.setCustomerEmail(customer.email());
        order.setCustomerName(customer.fullName());
        Customer.DeliveryAddress a = customer.address();
        order.setShippingAddress(new ShippingAddress(a.street(), a.city(), a.state(), a.country(), a.zipcode()));
        for (CartItem item : items) {
            ProductInfo product = catalog.findActiveProduct(item.getProductId())
                    .orElseThrow(() -> BusinessException.invalid("Product Unavailable",
                            "'" + item.getProductName() + "' is no longer available; remove it from your cart"));
            order.addItem(new OrderItem(product.id(), product.name(), item.getQuantity(), product.price()));
        }
        order.setTotalAmount(order.getItems().stream().map(OrderItem::subTotal).reduce(BigDecimal.ZERO, BigDecimal::add));

        catalog.reserve(stockLines(order));      // same transaction: all-or-nothing with the order
        orders.save(order);
        cart.deleteAllByUserId(userId);
        events.publishEvent(new OrderPlaced(order.getId(), userId, order.getCustomerEmail(), order.getCustomerName(),
                order.getTotalAmount(), order.getItems().stream()
                .map(i -> new OrderPlaced.Line(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList()));
        log.info("Order {} placed by user {} for {}", order.getId(), userId, order.getTotalAmount());
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> myOrders(long userId, Pageable pageable) {
        return orders.findByUserId(userId, pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse myOrder(long userId, long orderId) {
        return OrderResponse.from(own(userId, orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> allOrders(Pageable pageable) {
        return orders.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse cancel(long userId, long orderId) {
        Order order = own(userId, orderId);
        transition(order, OrderStatus.CANCELLED, "Cancelled by customer");
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse changeStatus(long orderId, OrderStatus status) {
        Order order = orders.findById(orderId).orElseThrow(() -> notFound(orderId));
        transition(order, status, status == OrderStatus.CANCELLED ? "Cancelled by store" : null);
        return OrderResponse.from(order);
    }

    // ---------------- OrderPayments (called by the payment module) ----------------

    @Override
    @Transactional
    public void paymentSucceeded(long orderId) {
        orders.findById(orderId)
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .ifPresentOrElse(o -> transition(o, OrderStatus.CONFIRMED, "Payment received"),
                        () -> log.info("Payment success for order {} ignored (not pending)", orderId));
    }

    @Override
    @Transactional
    public void paymentFailed(long orderId, String reason) {
        orders.findById(orderId)
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .ifPresentOrElse(o -> transition(o, OrderStatus.CANCELLED, "Payment failed: " + reason),
                        () -> log.info("Payment failure for order {} ignored (not pending)", orderId));
    }

    /** State machine + stock release + event, all in the caller's transaction. */
    private void transition(Order order, OrderStatus next, String reason) {
        OrderStatus previous = order.getStatus();
        if (!previous.canTransitionTo(next)) {
            throw BusinessException.invalid("Invalid Status Change",
                    "Order " + order.getId() + " cannot move from " + previous + " to " + next);
        }
        order.setStatus(next);
        order.setStatusReason(reason);
        if (next == OrderStatus.CANCELLED) {
            catalog.release(stockLines(order));
        }
        orders.saveAndFlush(order);
        events.publishEvent(new OrderStatusChanged(order.getId(), order.getUserId(), order.getCustomerEmail(),
                order.getCustomerName(), next, previous, order.getTotalAmount(), reason));
        log.info("Order {}: {} -> {}", order.getId(), previous, next);
    }

    private List<StockLine> stockLines(Order order) {
        return order.getItems().stream().map(i -> new StockLine(i.getProductId(), i.getQuantity())).toList();
    }

    private Order own(long userId, long orderId) {
        // same 404 for "missing" and "someone else's": order ids cannot be probed
        return orders.findByIdAndUserId(orderId, userId).orElseThrow(() -> notFound(orderId));
    }

    private BusinessException notFound(long orderId) {
        return BusinessException.notFound("Order " + orderId + " not found");
    }
}
