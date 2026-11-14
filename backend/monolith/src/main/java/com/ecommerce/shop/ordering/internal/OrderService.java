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
import com.ecommerce.shop.ordering.PaymentMethod;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ecommerce.shop.ordering.internal.OrderingDtos.*;

/**
 * Checkout and order lifecycle. Stock reservation, the order, clearing the cart and the
 * OrderPlaced event all commit in ONE local transaction. Payment happens asynchronously after
 * commit (payment module); cash-on-delivery orders are confirmed without a charge.
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
    public OrderResponse checkout(long userId, CheckoutRequest request) {
        CheckoutRequest checkout = request == null ? new CheckoutRequest(null, null, null) : request;
        List<CartItem> items = cart.findByUserIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            throw BusinessException.invalid("Empty Cart", "Your cart is empty");
        }
        Customer customer = customers.findCustomer(userId)
                .orElseThrow(() -> BusinessException.unauthorized("Account not found"));

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(checkout.paymentMethod() == null ? PaymentMethod.CARD : checkout.paymentMethod());
        order.setPaymentStatus(OrderPaymentStatus.PENDING);
        order.setStatusReason(order.getPaymentMethod() == PaymentMethod.CASH ? "Confirming your order" : "Awaiting payment");
        order.setCustomerEmail(customer.email());
        order.setCustomerName(customer.fullName());
        order.setNotes(checkout.notes());
        applyDeliveryAddress(order, checkout.shippingAddress(), customer);

        for (CartItem item : items) {
            ProductInfo product = catalog.findActiveProduct(item.getProductId())
                    .orElseThrow(() -> BusinessException.invalid("Product Unavailable",
                            "'" + item.getProductName() + "' is no longer available; remove it from your cart"));
            order.addItem(new OrderItem(product.id(), product.name(), item.getSize(), product.imageUrl(),
                    item.getQuantity(), product.price()));
        }
        order.setTotalAmount(order.getItems().stream().map(OrderItem::subTotal).reduce(BigDecimal.ZERO, BigDecimal::add));

        catalog.reserve(stockLines(order));      // same transaction: all-or-nothing with the order
        orders.save(order);
        cart.deleteAllByUserId(userId);
        events.publishEvent(new OrderPlaced(order.getId(), userId, order.getCustomerEmail(), order.getCustomerName(),
                order.getTotalAmount(), order.getPaymentMethod(), order.getItems().stream()
                .map(i -> new OrderPlaced.Line(i.getProductId(), i.getProductName(), i.getSize(), i.getQuantity(), i.getUnitPrice()))
                .toList()));
        log.info("Order {} placed by user {} for {} ({})", order.getId(), userId, order.getTotalAmount(), order.getPaymentMethod());
        return OrderResponse.from(order);
    }

    private void applyDeliveryAddress(Order order, DeliveryAddress given, Customer customer) {
        if (given != null) {
            order.setShippingAddress(new ShippingAddress(given.street().trim(), given.city().trim(), given.state(),
                    given.country().trim(), given.zipcode().trim()));
            order.setRecipientName(given.lastName() == null || given.lastName().isBlank()
                    ? given.firstName().trim() : given.firstName().trim() + " " + given.lastName().trim());
            order.setRecipientPhone(given.phone());
            return;
        }
        Customer.DeliveryAddress a = customer.address();
        if (a == null) {
            throw BusinessException.invalid("Profile Incomplete",
                    "Enter a delivery address or save one in your profile before placing an order");
        }
        order.setShippingAddress(new ShippingAddress(a.street(), a.city(), a.state(), a.country(), a.zipcode()));
        order.setRecipientName(customer.fullName());
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
        pending(orderId).ifPresent(o -> {
            o.setPaymentStatus(OrderPaymentStatus.PAID);
            transition(o, OrderStatus.CONFIRMED, "Payment received");
        });
    }

    @Override
    @Transactional
    public void paymentFailed(long orderId, String reason) {
        pending(orderId).ifPresent(o -> {
            o.setPaymentStatus(OrderPaymentStatus.FAILED);
            transition(o, OrderStatus.CANCELLED, "Payment failed: " + reason);
        });
    }

    @Override
    @Transactional
    public void cashOnDeliveryAccepted(long orderId) {
        pending(orderId).ifPresent(o -> {
            o.setPaymentStatus(OrderPaymentStatus.CASH_ON_DELIVERY);
            transition(o, OrderStatus.CONFIRMED, "Cash on delivery");
        });
    }

    @Override
    @Transactional
    public void paymentRefunded(long orderId) {
        orders.findById(orderId).ifPresent(o -> o.setPaymentStatus(OrderPaymentStatus.REFUNDED));
    }

    private java.util.Optional<Order> pending(long orderId) {
        java.util.Optional<Order> order = orders.findById(orderId).filter(o -> o.getStatus() == OrderStatus.PENDING);
        if (order.isEmpty()) {
            log.info("Payment outcome for order {} ignored (not pending)", orderId);
        }
        return order;
    }

    /** State machine + stock release + payment status + event, all in the caller's transaction. */
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
            if (order.getPaymentStatus() == OrderPaymentStatus.CASH_ON_DELIVERY
                    || order.getPaymentStatus() == OrderPaymentStatus.PENDING) {
                order.setPaymentStatus(OrderPaymentStatus.NOT_CHARGED);
            }
        }
        if (next == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.CASH) {
            order.setPaymentStatus(OrderPaymentStatus.PAID);   // cash collected on delivery
        }
        orders.saveAndFlush(order);
        events.publishEvent(new OrderStatusChanged(order.getId(), order.getUserId(), order.getCustomerEmail(),
                order.getCustomerName(), next, previous, order.getTotalAmount(), reason));
        log.info("Order {}: {} -> {}", order.getId(), previous, next);
    }

    /** One line per product (sizes of the same product share its stock). */
    private List<StockLine> stockLines(Order order) {
        Map<Long, Integer> perProduct = order.getItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getProductId, Collectors.summingInt(OrderItem::getQuantity)));
        Collection<Map.Entry<Long, Integer>> entries = perProduct.entrySet();
        return entries.stream().map(e -> new StockLine(e.getKey(), e.getValue())).toList();
    }

    private Order own(long userId, long orderId) {
        // same 404 for "missing" and "someone else's": order ids cannot be probed
        return orders.findByIdAndUserId(orderId, userId).orElseThrow(() -> notFound(orderId));
    }

    private BusinessException notFound(long orderId) {
        return BusinessException.notFound("Order " + orderId + " not found");
    }
}
