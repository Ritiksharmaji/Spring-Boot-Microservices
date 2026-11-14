package com.app_ecom.order;

import com.app_ecom.clients.ProductServiceClient;
import com.app_ecom.clients.UserServiceClient;
import com.app_ecom.dto.AddressDTO;
import com.app_ecom.dto.ProductResponse;
import com.app_ecom.dto.StockRequest;
import com.app_ecom.dto.UserResponse;
import com.app_ecom.messaging.PaymentResultEvent;
import com.app_ecom.model.OrderStatus;
import com.app_ecom.outbox.OutboxEvent;
import com.app_ecom.outbox.OutboxPublisher;
import com.app_ecom.outbox.OutboxRepository;
import com.app_ecom.repository.CartItemRepository;
import com.app_ecom.service.OrderService;
import com.app_ecom.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:orders;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.outbox.poll-interval-ms=3600000"   // tests trigger the outbox publisher explicitly
})
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    static final String ALICE = "kc-alice";
    static final String BOB = "kc-bob";

    @Autowired MockMvc mvc;
    @Autowired OrderRepository orderRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired CircuitBreakerRegistry circuitBreakers;
    @Autowired OutboxRepository outboxRepository;
    @Autowired OutboxPublisher outboxPublisher;
    @Autowired OrderService orderService;

    @MockitoBean ProductServiceClient productClient;
    @MockitoBean UserServiceClient userClient;
    @MockitoBean StreamBridge streamBridge;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        circuitBreakers.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        reset(productClient, userClient, streamBridge);
        when(productClient.getProduct(1L)).thenReturn(product(1L, "Keyboard", "499.50", 10));
        when(productClient.getProduct(2L)).thenReturn(product(2L, "Mouse", "100.00", 3));
        when(userClient.getUserByKeycloakId(anyString())).thenReturn(customerWithAddress());
        when(streamBridge.send(anyString(), any())).thenReturn(true);
    }

    // ---------------- cart ----------------

    @Test
    void cartUsesLivePricesAndCorrectTotals() throws Exception {
        addToCart(ALICE, 1, 2).andExpect(status().isCreated());
        addToCart(ALICE, 2, 1)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalItems", is(3)))
                .andExpect(jsonPath("$.totalAmount", is(1099.00)))          // 2 x 499.50 + 1 x 100
                .andExpect(jsonPath("$.items[0].unitPrice", is(499.50)))
                .andExpect(jsonPath("$.items[0].subTotal", is(999.00)));

        addToCart(ALICE, 1, 1).andExpect(jsonPath("$.items[0].quantity", is(3)));  // same product merges
    }

    @Test
    void cartRejectsMoreThanAvailableStockAndUnknownProducts() throws Exception {
        addToCart(ALICE, 2, 4)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Insufficient Stock")));

        when(productClient.getProduct(99L)).thenThrow(clientError(HttpStatus.NOT_FOUND));
        addToCart(ALICE, 99, 1).andExpect(status().isNotFound());

        mvc.perform(post("/api/cart/items").header("X-User-ID", ALICE)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void requestsWithoutUserContextAreRejected() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    // ---------------- checkout ----------------

    @Test
    void placeOrder_reservesStock_savesSnapshot_clearsCart_writesOutboxEvent() throws Exception {
        addToCart(ALICE, 1, 2);
        addToCart(ALICE, 2, 1);

        String json = mvc.perform(post("/api/orders").header("X-User-ID", ALICE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))           // waits for payment-service
                .andExpect(jsonPath("$.statusReason", is("Awaiting payment")))
                .andExpect(jsonPath("$.totalAmount", is(1099.00)))
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items[0].productName", is("Keyboard")))
                .andExpect(jsonPath("$.shippingAddress.city", is("Pune")))
                .andReturn().getResponse().getContentAsString();
        long orderId = Long.parseLong(json.replaceAll("^\\{\"id\":(\\d+).*", "$1"));

        ArgumentCaptor<StockRequest> reserved = ArgumentCaptor.forClass(StockRequest.class);
        verify(productClient).reserveStock(reserved.capture());
        assertThat(reserved.getValue().items()).extracting("productId", "quantity")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, 2), org.assertj.core.groups.Tuple.tuple(2L, 1));

        List<OutboxEvent> events = outboxRepository.findByAggregateIdOrderByIdAsc("order-" + orderId);
        assertThat(events).extracting(OutboxEvent::getEventType).containsExactly("OrderCreated");
        assertThat(events.get(0).getBinding()).isEqualTo("orderCreated-out-0");
        assertThat(events.get(0).getPayload()).contains("\"totalAmount\":1099.00", "\"customerEmail\":\"alice@example.com\"");

        mvc.perform(get("/api/cart").header("X-User-ID", ALICE)).andExpect(jsonPath("$.totalItems", is(0)));
        mvc.perform(get("/api/orders").header("X-User-ID", ALICE))
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    // ---------------- saga ----------------

    @Test
    void paymentCompleted_confirmsOrder_andIgnoresDuplicates() throws Exception {
        long orderId = placeOrderFor(ALICE);

        orderService.handlePaymentResult(payment(orderId, PaymentResultEvent.Status.COMPLETED, null));
        orderService.handlePaymentResult(payment(orderId, PaymentResultEvent.Status.FAILED, "late duplicate"));

        mvc.perform(get("/api/orders/{id}", orderId).header("X-User-ID", ALICE))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.statusReason", is("Payment received")));
        verify(productClient, never()).releaseStock(any());
        assertThat(outboxRepository.findByAggregateIdOrderByIdAsc("order-" + orderId))
                .extracting(OutboxEvent::getEventType).containsExactly("OrderCreated", "OrderStatusChanged");
    }

    @Test
    void paymentFailed_cancelsOrder_andReleasesStock() throws Exception {
        long orderId = placeOrderFor(ALICE);

        orderService.handlePaymentResult(payment(orderId, PaymentResultEvent.Status.FAILED, "Card declined"));

        mvc.perform(get("/api/orders/{id}", orderId).header("X-User-ID", ALICE))
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.statusReason", is("Payment failed: Card declined")));
        verify(productClient).releaseStock(any());
        OutboxEvent statusEvent = outboxRepository.findByAggregateIdOrderByIdAsc("order-" + orderId).get(1);
        assertThat(statusEvent.getBinding()).isEqualTo("orderStatus-out-0");
        assertThat(statusEvent.getPayload()).contains("\"status\":\"CANCELLED\"", "\"previousStatus\":\"PENDING\"");
    }

    @Test
    void outboxPublisherSendsPendingEventsOnceAndMarksThemPublished() throws Exception {
        placeOrderFor(ALICE);
        outboxPublisher.publishPending();
        clearInvocations(streamBridge);

        outboxPublisher.publishPending();   // nothing left to send

        verify(streamBridge, never()).send(anyString(), any());
        assertThat(outboxRepository.countByPublishedAtIsNull()).isZero();
    }

    @Test
    void outboxKeepsEventsWhenBrokerIsDown() throws Exception {
        when(streamBridge.send(anyString(), any())).thenReturn(false);
        placeOrderFor(ALICE);

        outboxPublisher.publishPending();

        assertThat(outboxRepository.countByPublishedAtIsNull()).isEqualTo(1);
        assertThat(outboxRepository.findAll().get(0).getAttempts()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void placeOrder_usesCurrentPriceNotTheOneInTheCart() throws Exception {
        addToCart(ALICE, 1, 1);
        when(productClient.getProduct(1L)).thenReturn(product(1L, "Keyboard", "450.00", 10));

        mvc.perform(post("/api/orders").header("X-User-ID", ALICE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount", is(450.00)));
    }

    @Test
    void placeOrder_failsCleanlyWhenStockIsGoneMeanwhile() throws Exception {
        addToCart(ALICE, 1, 2);
        doThrow(clientError(HttpStatus.CONFLICT)).when(productClient).reserveStock(any());

        mvc.perform(post("/api/orders").header("X-User-ID", ALICE))
                .andExpect(status().isConflict());

        verify(productClient, times(1)).reserveStock(any());  // reservation is never retried
        assertThat(orderRepository.count()).isZero();
        assertThat(cartItemRepository.count()).isEqualTo(1);  // cart kept so the user can adjust it
    }

    @Test
    void placeOrder_requiresProfileWithAddressAndNonEmptyCart() throws Exception {
        mvc.perform(post("/api/orders").header("X-User-ID", ALICE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Empty Cart")));

        addToCart(ALICE, 1, 1);
        when(userClient.getUserByKeycloakId(ALICE)).thenThrow(clientError(HttpStatus.NOT_FOUND));
        mvc.perform(post("/api/orders").header("X-User-ID", ALICE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Profile Incomplete")));
        verify(productClient, never()).reserveStock(any());
    }

    @Test
    void productServiceDown_returns503AfterRetries() throws Exception {
        when(productClient.getProduct(1L)).thenThrow(new ResourceAccessException("Connection refused"));

        addToCart(ALICE, 1, 1)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title", is("Service Unavailable")));
        verify(productClient, times(3)).getProduct(1L);   // retry: 3 attempts
    }

    // ---------------- after checkout ----------------

    @Test
    void customerCanCancelOwnOrder_stockIsReleased_onlyOnce() throws Exception {
        long orderId = placeOrderFor(ALICE);

        mvc.perform(post("/api/orders/{id}/cancel", orderId).header("X-User-ID", ALICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
        verify(productClient).releaseStock(any());

        mvc.perform(post("/api/orders/{id}/cancel", orderId).header("X-User-ID", ALICE))
                .andExpect(status().isUnprocessableEntity());
        verify(productClient, times(1)).releaseStock(any());
    }

    @Test
    void usersCannotSeeOrCancelOtherUsersOrders() throws Exception {
        long orderId = placeOrderFor(ALICE);

        mvc.perform(get("/api/orders/{id}", orderId).header("X-User-ID", BOB)).andExpect(status().isNotFound());
        mvc.perform(post("/api/orders/{id}/cancel", orderId).header("X-User-ID", BOB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders").header("X-User-ID", BOB)).andExpect(jsonPath("$.content.length()", is(0)));
    }

    @Test
    void onlyAdminsChangeStatus_andOnlyValidTransitions() throws Exception {
        long orderId = placeOrderFor(ALICE);
        updateStatus(orderId, "SHIPPED", "ADMIN").andExpect(status().isUnprocessableEntity()); // not paid yet
        orderService.handlePaymentResult(payment(orderId, PaymentResultEvent.Status.COMPLETED, null));

        updateStatus(orderId, "SHIPPED", "CUSTOMER").andExpect(status().isForbidden());
        updateStatus(orderId, "SHIPPED", "ADMIN").andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")));
        updateStatus(orderId, "CONFIRMED", "ADMIN").andExpect(status().isUnprocessableEntity());
        updateStatus(orderId, "DELIVERED", "CUSTOMER,ADMIN").andExpect(status().isOk());

        mvc.perform(post("/api/orders/{id}/cancel", orderId).header("X-User-ID", ALICE))
                .andExpect(status().isUnprocessableEntity());   // delivered orders cannot be cancelled

        mvc.perform(get("/api/orders/admin").header("X-User-Roles", "CUSTOMER")).andExpect(status().isForbidden());
        mvc.perform(get("/api/orders/admin").header("X-User-Roles", "ADMIN"))
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    // ---------------- helpers ----------------

    private ResultActions addToCart(String user, long productId, int qty) throws Exception {
        return mvc.perform(post("/api/cart/items").header("X-User-ID", user)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":%d,\"quantity\":%d}".formatted(productId, qty)));
    }

    private ResultActions updateStatus(long orderId, String status, String roles) throws Exception {
        return mvc.perform(patch("/api/orders/{id}/status", orderId).header("X-User-Roles", roles)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"));
    }

    private long placeOrderFor(String user) throws Exception {
        addToCart(user, 1, 1);
        String json = mvc.perform(post("/api/orders").header("X-User-ID", user))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
    }

    private static PaymentResultEvent payment(long orderId, PaymentResultEvent.Status status, String reason) {
        return new PaymentResultEvent(orderId, ALICE, 1L, status, new BigDecimal("499.50"), reason, LocalDateTime.now());
    }

    private static ProductResponse product(long id, String name, String price, int stock) {
        ProductResponse p = new ProductResponse();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStockQuantity(stock);
        p.setActive(true);
        return p;
    }

    private static UserResponse customerWithAddress() {
        AddressDTO a = new AddressDTO();
        a.setStreet("MG Road");
        a.setCity("Pune");
        a.setState("MH");
        a.setCountry("India");
        a.setZipcode("411001");
        UserResponse u = new UserResponse();
        u.setId("u1");
        u.setFirstName("Alice");
        u.setEmail("alice@example.com");
        u.setAddress(a);
        return u;
    }

    private static HttpClientErrorException clientError(HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY,
                new byte[0], StandardCharsets.UTF_8);
    }
}
