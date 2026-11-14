package com.app_ecom.order;

import com.app_ecom.clients.ProductServiceClient;
import com.app_ecom.clients.UserServiceClient;
import com.app_ecom.dto.AddressDTO;
import com.app_ecom.dto.CartItemRequest;
import com.app_ecom.dto.OrderResponse;
import com.app_ecom.dto.ProductResponse;
import com.app_ecom.dto.UserResponse;
import com.app_ecom.messaging.PaymentResultEvent;
import com.app_ecom.model.OrderStatus;
import com.app_ecom.outbox.OutboxPublisher;
import com.app_ecom.outbox.OutboxRepository;
import com.app_ecom.service.CartService;
import com.app_ecom.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Full order lifecycle on a real PostgreSQL (schema, numeric precision, outbox JSON column,
 * transactions). Downstream HTTP clients and the broker are mocked. Skipped without Docker.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.outbox.poll-interval-ms=3600000"
})
@Testcontainers(disabledWithoutDocker = true)
class OrderPostgresContainerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired CartService cartService;
    @Autowired OrderService orderService;
    @Autowired OutboxPublisher outboxPublisher;
    @Autowired OutboxRepository outboxRepository;

    @MockitoBean ProductServiceClient productClient;
    @MockitoBean UserServiceClient userClient;
    @MockitoBean StreamBridge streamBridge;

    @Test
    void checkoutPaymentAndOutboxOnPostgres() {
        ProductResponse p = new ProductResponse();
        p.setId(1L);
        p.setName("Keyboard");
        p.setPrice(new BigDecimal("1234.56"));
        p.setStockQuantity(5);
        p.setActive(true);
        when(productClient.getProduct(1L)).thenReturn(p);
        AddressDTO a = new AddressDTO();
        a.setStreet("MG Road"); a.setCity("Pune"); a.setCountry("India"); a.setZipcode("411001");
        UserResponse u = new UserResponse();
        u.setFirstName("Alice"); u.setEmail("alice@example.com"); u.setAddress(a);
        when(userClient.getUserByKeycloakId(anyString())).thenReturn(u);
        when(streamBridge.send(anyString(), any())).thenReturn(true);

        cartService.addToCart("kc-alice", new CartItemRequest(1L, 2));
        OrderResponse order = orderService.placeOrder("kc-alice");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.totalAmount()).isEqualByComparingTo("2469.12");

        orderService.handlePaymentResult(new PaymentResultEvent(order.id(), "kc-alice", 1L,
                PaymentResultEvent.Status.COMPLETED, order.totalAmount(), null, LocalDateTime.now()));
        assertThat(orderService.getMyOrder("kc-alice", order.id()).status()).isEqualTo(OrderStatus.CONFIRMED);

        outboxPublisher.publishPending();
        assertThat(outboxRepository.countByPublishedAtIsNull()).isZero();
        assertThat(outboxRepository.count()).isEqualTo(2);
    }
}
