package com.ecommerce.shop;

import com.ecommerce.shop.notification.Notification;
import com.ecommerce.shop.notification.NotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The whole shopping journey through the modules, including the asynchronous event listeners. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EventPublicationRegistry registry;
    @MockitoBean NotificationSender sender;

    TestClient api;
    String admin;
    String customer;
    long product;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestClient(mvc, json);
        admin = api.login("admin@test.local", "Admin12345");
        customer = api.register("c-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local");
        api.saveAddress(customer);
        product = api.createProduct(admin, "Keyboard", "499.50", 5);
    }

    private String orderStatus(String token, long orderId) throws Exception {
        return api.read(api.call("GET", "/api/orders/" + orderId, token, null)).get("status").asText();
    }

    private void awaitStatus(String token, long orderId, String expected) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(orderStatus(token, orderId)).isEqualTo(expected));
    }

    private int stock(long productId) throws Exception {
        return api.read(api.call("GET", "/api/products/" + productId, null, null)).get("stockQuantity").asInt();
    }

    private long checkout(String token, long productId, int qty) throws Exception {
        api.call("POST", "/api/cart/items", token, "{\"productId\":" + productId + ",\"quantity\":" + qty + "}")
                .andExpect(status().isCreated());
        return api.read(api.call("POST", "/api/orders", token, null).andExpect(status().isCreated())).get("id").asLong();
    }

    @Test
    void cartUsesLivePricesTotalsAndStockChecks() throws Exception {
        long mouse = api.createProduct(admin, "Mouse", "100.00", 3);
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + product + ",\"quantity\":2}");
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + mouse + ",\"quantity\":1}")
                .andExpect(jsonPath("$.totalItems", is(3)))
                .andExpect(jsonPath("$.totalAmount", is(1099.0)));
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + mouse + ",\"quantity\":5}")
                .andExpect(status().isConflict());
        api.call("PUT", "/api/cart/items/" + mouse, customer, "{\"quantity\":2}")
                .andExpect(jsonPath("$.totalAmount", is(1199.0)));
        api.call("DELETE", "/api/cart/items/" + mouse, customer, null).andExpect(status().isNoContent());
        api.call("POST", "/api/cart/items", customer, "{\"productId\":999999,\"quantity\":1}")
                .andExpect(status().isNotFound());
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + product + ",\"quantity\":0}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutIsPendingThenConfirmedByPayment_stockReduced_emailsSent() throws Exception {
        long orderId = checkout(customer, product, 2);

        assertThat(stock(product)).isEqualTo(3);                      // reserved in the checkout transaction
        awaitStatus(customer, orderId, "CONFIRMED");                  // payment module -> OrderPayments
        api.call("GET", "/api/payments/orders/" + orderId, customer, null)
                .andExpect(jsonPath("$.status", is("COMPLETED")));
        api.call("GET", "/api/cart", customer, null).andExpect(jsonPath("$.totalItems", is(0)));

        ArgumentCaptor<Notification> mails = ArgumentCaptor.forClass(Notification.class);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(sender, atLeastOnce()).send(mails.capture());
            assertThat(mails.getAllValues()).extracting(Notification::subject)
                    .contains("Order #" + orderId + " received", "Order #" + orderId + " confirmed");
        });
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(registry.findIncompletePublications()).isEmpty());   // every event delivered
    }

    @Test
    void declinedPaymentCancelsTheOrderAndReturnsStock() throws Exception {
        long laptop = api.createProduct(admin, "Laptop", "60000.00", 1);
        long orderId = checkout(customer, laptop, 1);
        assertThat(stock(laptop)).isZero();

        awaitStatus(customer, orderId, "CANCELLED");
        api.call("GET", "/api/orders/" + orderId, customer, null)
                .andExpect(jsonPath("$.statusReason", is("Payment failed: Amount 60000.00 exceeds the card limit of 50000.00")));
        assertThat(stock(laptop)).isEqualTo(1);
        api.call("GET", "/api/payments/orders/" + orderId, customer, null).andExpect(jsonPath("$.status", is("FAILED")));
    }

    @Test
    void customerCancelAfterPaymentRefundsAndReturnsStockOnce() throws Exception {
        long orderId = checkout(customer, product, 1);
        awaitStatus(customer, orderId, "CONFIRMED");

        api.call("POST", "/api/orders/" + orderId + "/cancel", customer, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.status", is("CANCELLED")));
        assertThat(stock(product)).isEqualTo(5);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                api.call("GET", "/api/payments/orders/" + orderId, customer, null)
                        .andExpect(jsonPath("$.status", is("REFUNDED"))));

        api.call("POST", "/api/orders/" + orderId + "/cancel", customer, null)
                .andExpect(status().isUnprocessableEntity());
        assertThat(stock(product)).isEqualTo(5);
    }

    @Test
    void checkoutRulesAndIsolation() throws Exception {
        api.call("POST", "/api/orders", customer, null)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title", is("Empty Cart")));

        String noAddress = api.register("n-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local");
        api.call("POST", "/api/cart/items", noAddress, "{\"productId\":" + product + ",\"quantity\":1}");
        api.call("POST", "/api/orders", noAddress, null)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title", is("Profile Incomplete")));
        assertThat(stock(product)).isEqualTo(5);

        long orderId = checkout(customer, product, 1);
        api.call("GET", "/api/orders/" + orderId, noAddress, null).andExpect(status().isNotFound());
        api.call("POST", "/api/orders/" + orderId + "/cancel", noAddress, null).andExpect(status().isNotFound());
        api.call("GET", "/api/payments/orders/" + orderId, noAddress, null).andExpect(status().isNotFound());
        api.call("GET", "/api/orders", noAddress, null).andExpect(jsonPath("$.content.length()", is(0)));
    }

    @Test
    void adminFulfilmentFollowsTheStateMachine() throws Exception {
        long orderId = checkout(customer, product, 1);
        awaitStatus(customer, orderId, "CONFIRMED");

        api.call("PATCH", "/api/orders/" + orderId + "/status", customer, "{\"status\":\"SHIPPED\"}")
                .andExpect(status().isForbidden());
        api.call("PATCH", "/api/orders/" + orderId + "/status", admin, "{\"status\":\"SHIPPED\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status", is("SHIPPED")));
        api.call("PATCH", "/api/orders/" + orderId + "/status", admin, "{\"status\":\"CONFIRMED\"}")
                .andExpect(status().isUnprocessableEntity());
        api.call("PATCH", "/api/orders/" + orderId + "/status", admin, "{\"status\":\"DELIVERED\"}")
                .andExpect(status().isOk());
        api.call("POST", "/api/orders/" + orderId + "/cancel", customer, null)
                .andExpect(status().isUnprocessableEntity());

        api.call("GET", "/api/orders/admin", admin, null).andExpect(status().isOk());
        api.call("GET", "/api/orders/admin", customer, null).andExpect(status().isForbidden());
        api.call("GET", "/api/payments/admin", admin, null).andExpect(status().isOk());
    }
}
