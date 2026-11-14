package com.app_ecom.service;

import com.app_ecom.dto.*;
import com.app_ecom.exception.InvalidRequestException;
import com.app_ecom.exception.ResourceNotFoundException;
import com.app_ecom.model.*;
import com.app_ecom.repository.CartItemRepository;
import com.app_ecom.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    static final String ORDER_CREATED_BINDING = "createOrder-out-0";

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductGateway productGateway;
    private final UserGateway userGateway;
    private final StreamBridge streamBridge;
    private final TransactionTemplate transactionTemplate;

    /**
     * Checkout: validates the cart and the customer profile, re-reads live prices,
     * reserves stock atomically in product-service, then persists the order.
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
                order.setStatus(OrderStatus.CONFIRMED);
                order.setShippingAddress(toShippingAddress(customer.getAddress()));
                lines.forEach(order::addItem);
                order.setTotalAmount(lines.stream().map(OrderItem::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
                Order persisted = orderRepository.save(order);
                cartItemRepository.deleteAllByUserId(userId);
                return persisted;
            });
        } catch (RuntimeException e) {
            log.error("Saving order for user {} failed; releasing reserved stock", userId, e);
            releaseQuietly(stock);
            throw e;
        }

        publishOrderCreated(saved);
        log.info("Order {} placed by {} for {}", saved.getId(), userId, saved.getTotalAmount());
        return toResponse(saved);
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
        return changeStatus(orderId, OrderStatus.CANCELLED, userId);
    }

    /** Admin moves an order through its lifecycle (e.g. CONFIRMED -> SHIPPED -> DELIVERED). */
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        return changeStatus(orderId, newStatus, null);
    }

    private OrderResponse changeStatus(Long orderId, OrderStatus newStatus, String ownerId) {
        record Result(OrderResponse response, StockRequest toRelease) {}

        Result result = transactionTemplate.execute(tx -> {
            Order order = ownerId == null
                    ? orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId))
                    : findOwnOrder(ownerId, orderId);
            if (!order.getStatus().canTransitionTo(newStatus)) {
                throw new InvalidRequestException("Invalid Status Change",
                        "Order " + orderId + " cannot move from " + order.getStatus() + " to " + newStatus);
            }
            order.setStatus(newStatus);
            orderRepository.saveAndFlush(order);
            StockRequest release = newStatus == OrderStatus.CANCELLED ? toStockRequest(order.getItems()) : null;
            return new Result(toResponse(order), release);
        });

        if (result.toRelease() != null) {
            releaseQuietly(result.toRelease());
        }
        log.info("Order {} -> {}", orderId, newStatus);
        return result.response();
    }

    private Order findOwnOrder(String userId, Long orderId) {
        // Same 404 for "missing" and "someone else's" so order ids cannot be probed.
        return orderRepository.findByIdAndUserId(orderId, userId).orElseThrow(() -> notFound(orderId));
    }

    private void releaseQuietly(StockRequest stock) {
        try {
            productGateway.releaseStock(stock);
        } catch (RuntimeException e) {
            // Needs manual reconciliation; replaced by an event-driven saga in branch-18-3.
            log.error("Could not release stock {}", stock, e);
        }
    }

    private void publishOrderCreated(Order order) {
        OrderCreateEvent event = new OrderCreateEvent(order.getId(), order.getUserId(), order.getStatus(),
                toItemDtos(order.getItems()), order.getTotalAmount(), order.getCreateAt());
        try {
            if (!streamBridge.send(ORDER_CREATED_BINDING, event)) {
                log.warn("Order-created event for order {} was not accepted by the broker", order.getId());
            }
        } catch (RuntimeException e) {
            // The order itself is valid; a missed notification must not fail the checkout.
            log.error("Publishing order-created event for order {} failed", order.getId(), e);
        }
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
