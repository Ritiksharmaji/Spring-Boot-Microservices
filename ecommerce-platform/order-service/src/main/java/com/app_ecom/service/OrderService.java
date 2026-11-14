package com.app_ecom.service;

import com.app_ecom.dto.*;
import com.app_ecom.exception.InvalidRequestException;
import com.app_ecom.exception.ResourceNotFoundException;
import com.app_ecom.messaging.OrderCreatedEvent;
import com.app_ecom.messaging.OrderStatusChangedEvent;
import com.app_ecom.messaging.PaymentResultEvent;
import com.app_ecom.model.*;
import com.app_ecom.outbox.OutboxWriter;
import com.app_ecom.repository.CartItemRepository;
import com.app_ecom.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order lifecycle and the order side of the checkout saga:
 *
 *   placeOrder        -> stock reserved, order PENDING, OrderCreated (outbox)
 *   payment COMPLETED -> CONFIRMED, OrderStatusChanged
 *   payment FAILED    -> CANCELLED, stock released, OrderStatusChanged
 *   customer cancel   -> CANCELLED, stock released, OrderStatusChanged (payment-service refunds)
 *   admin             -> SHIPPED -> DELIVERED, OrderStatusChanged
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    public static final String ORDER_CREATED_BINDING = "orderCreated-out-0";
    public static final String ORDER_STATUS_BINDING = "orderStatus-out-0";

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductGateway productGateway;
    private final UserGateway userGateway;
    private final OutboxWriter outbox;
    private final TransactionTemplate transactionTemplate;

    /**
     * Checkout: validates the cart and the customer profile, re-reads live prices,
     * reserves stock atomically in product-service, then persists the order as PENDING
     * together with its OrderCreated event (transactional outbox).
     * If persisting fails after stock was reserved, the reservation is released (compensation).
     * Remote calls are made outside the DB transaction so no connection is held during HTTP calls.
     */
    public OrderResponse placeOrder(String userId) {
        List<CartItem> cart = cartItemRepository.findByUserIdOrderByIdAsc(userId);
        if (cart.isEmpty()) {
            throw new InvalidRequestException("Empty Cart", "Your cart is empty");
        }

        UserResponse customer = userGateway.getUser(userId)
                .filter(u -> u.getAddress() != null)
                .orElseThrow(() -> new InvalidRequestException("Profile Incomplete",
                        "Add your profile and delivery address (PUT /api/users/me) before placing an order"));

        List<OrderItem> lines = new ArrayList<>();
        for (CartItem cartItem : cart) {
            ProductResponse product = productGateway.getProduct(cartItem.getProductId())
                    .filter(p -> !Boolean.FALSE.equals(p.getActive()))
                    .orElseThrow(() -> new InvalidRequestException("Product Unavailable",
                            "'" + cartItem.getProductName() + "' is no longer available; remove it from your cart"));
            lines.add(new OrderItem(product.getId(), product.getName(), cartItem.getQuantity(), product.getPrice()));
        }

        StockRequest stock = toStockRequest(lines);
        productGateway.reserveStock(stock);

        Order saved;
        try {
            saved = transactionTemplate.execute(tx -> {
                Order order = new Order();
                order.setUserId(userId);
                order.setStatus(OrderStatus.PENDING);
                order.setStatusReason("Awaiting payment");
                order.setCustomerEmail(customer.getEmail());
                order.setCustomerName(fullName(customer));
                order.setShippingAddress(toShippingAddress(customer.getAddress()));
                lines.forEach(order::addItem);
                order.setTotalAmount(lines.stream().map(OrderItem::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
                Order persisted = orderRepository.save(order);
                cartItemRepository.deleteAllByUserId(userId);
                outbox.write(aggregateId(persisted), "OrderCreated", ORDER_CREATED_BINDING, toCreatedEvent(persisted));
                return persisted;
            });
        } catch (RuntimeException e) {
            log.error("Saving order for user {} failed; releasing reserved stock", userId, e);
            releaseQuietly(stock);
            throw e;
        }

        log.info("Order {} placed by {} for {} (awaiting payment)", saved.getId(), userId, saved.getTotalAmount());
        return toResponse(saved);
    }

    /**
     * Saga step triggered by payment-service. Idempotent: events for orders that are no
     * longer PENDING (duplicates, late deliveries) are ignored.
     */
    public void handlePaymentResult(PaymentResultEvent event) {
        boolean success = event.status() == PaymentResultEvent.Status.COMPLETED;
        OrderStatus target = success ? OrderStatus.CONFIRMED : OrderStatus.CANCELLED;
        String reason = success ? "Payment received" : "Payment failed: " + event.reason();

        StockRequest toRelease = transactionTemplate.execute(tx -> {
            Order order = orderRepository.findById(event.orderId()).orElse(null);
            if (order == null) {
                log.warn("Payment result for unknown order {} ignored", event.orderId());
                return null;
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                log.info("Duplicate/late payment result for order {} in status {} ignored",
                        order.getId(), order.getStatus());
                return null;
            }
            applyStatus(order, target, reason);
            return success ? null : toStockRequest(order.getItems());
        });

        if (toRelease != null) {
            releaseQuietly(toRelease);
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String userId, Long orderId) {
        return toResponse(findOwnOrder(userId, orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    /** Customer cancels their own order (only while PENDING/CONFIRMED). Stock goes back to the catalog. */
    public OrderResponse cancelMyOrder(String userId, Long orderId) {
        return changeStatus(orderId, OrderStatus.CANCELLED, userId, "Cancelled by customer");
    }

    /** Admin moves an order through its lifecycle (e.g. CONFIRMED -> SHIPPED -> DELIVERED). */
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        return changeStatus(orderId, newStatus, null,
                newStatus == OrderStatus.CANCELLED ? "Cancelled by store" : null);
    }

    private OrderResponse changeStatus(Long orderId, OrderStatus newStatus, String ownerId, String reason) {
        record Result(OrderResponse response, StockRequest toRelease) {}

        Result result = transactionTemplate.execute(tx -> {
            Order order = ownerId == null
                    ? orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId))
                    : findOwnOrder(ownerId, orderId);
            if (!order.getStatus().canTransitionTo(newStatus)) {
                throw new InvalidRequestException("Invalid Status Change",
                        "Order " + orderId + " cannot move from " + order.getStatus() + " to " + newStatus);
            }
            applyStatus(order, newStatus, reason);
            StockRequest release = newStatus == OrderStatus.CANCELLED ? toStockRequest(order.getItems()) : null;
            return new Result(toResponse(order), release);
        });

        if (result.toRelease() != null) {
            releaseQuietly(result.toRelease());
        }
        return result.response();
    }

    /** Changes status and records the OrderStatusChanged event in the same transaction. */
    private void applyStatus(Order order, OrderStatus newStatus, String reason) {
        OrderStatus previous = order.getStatus();
        order.setStatus(newStatus);
        order.setStatusReason(reason);
        orderRepository.saveAndFlush(order);
        outbox.write(aggregateId(order), "OrderStatusChanged", ORDER_STATUS_BINDING,
                new OrderStatusChangedEvent(order.getId(), order.getUserId(), order.getCustomerEmail(),
                        order.getCustomerName(), newStatus, previous, order.getTotalAmount(), reason,
                        LocalDateTime.now()));
        log.info("Order {}: {} -> {}{}", order.getId(), previous, newStatus, reason == null ? "" : " (" + reason + ")");
    }

    private Order findOwnOrder(String userId, Long orderId) {
        // Same 404 for "missing" and "someone else's" so order ids cannot be probed.
        return orderRepository.findByIdAndUserId(orderId, userId).orElseThrow(() -> notFound(orderId));
    }

    private void releaseQuietly(StockRequest stock) {
        try {
            productGateway.releaseStock(stock);
        } catch (RuntimeException e) {
            // Logged for manual reconciliation; product-service exposes the same release endpoint.
            log.error("Could not release stock {}", stock, e);
        }
    }

    private OrderCreatedEvent toCreatedEvent(Order order) {
        return new OrderCreatedEvent(order.getId(), order.getUserId(), order.getCustomerEmail(),
                order.getCustomerName(), order.getTotalAmount(),
                order.getItems().stream()
                        .map(i -> new OrderCreatedEvent.Item(i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                        .toList(),
                LocalDateTime.now());
    }

    private static String aggregateId(Order order) {
        return "order-" + order.getId();
    }

    private static String fullName(UserResponse u) {
        return u.getLastName() == null ? u.getFirstName() : u.getFirstName() + " " + u.getLastName();
    }

    private StockRequest toStockRequest(List<OrderItem> items) {
        return new StockRequest(items.stream().map(i -> new StockItem(i.getProductId(), i.getQuantity())).toList());
    }

    private ShippingAddress toShippingAddress(AddressDTO a) {
        return new ShippingAddress(a.getStreet(), a.getCity(), a.getState(), a.getCountry(), a.getZipcode());
    }

    private List<OrderItemDTO> toItemDtos(List<OrderItem> items) {
        return items.stream()
                .map(i -> new OrderItemDTO(i.getId(), i.getProductId(), i.getProductName(), i.getQuantity(),
                        i.getPrice(), i.getSubTotal()))
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        AddressDTO address = null;
        if (order.getShippingAddress() != null) {
            ShippingAddress s = order.getShippingAddress();
            address = new AddressDTO();
            address.setStreet(s.getStreet());
            address.setCity(s.getCity());
            address.setState(s.getState());
            address.setCountry(s.getCountry());
            address.setZipcode(s.getZipcode());
        }
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .statusReason(order.getStatusReason())
                .totalAmount(order.getTotalAmount())
                .items(toItemDtos(order.getItems()))
                .shippingAddress(address)
                .createdAt(order.getCreateAt())
                .updatedAt(order.getUpdateAt())
                .build();
    }

    private ResourceNotFoundException notFound(Long orderId) {
        return new ResourceNotFoundException("Order " + orderId + " not found");
    }
}
