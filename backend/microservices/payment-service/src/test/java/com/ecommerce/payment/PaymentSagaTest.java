package com.ecommerce.payment;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:payments;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.payment.max-amount=5000.00"
})
@Import(TestChannelBinderConfiguration.class)
@AutoConfigureMockMvc
class PaymentSagaTest {

    @Autowired InputDestination input;
    @Autowired OutputDestination output;
    @Autowired PaymentRepository repository;
    @Autowired ObjectMapper mapper;
    @Autowired MockMvc mvc;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        output.clear();
    }

    private void orderCreated(long orderId, String amount) {
        send("order.created", """
                {"orderId":%d,"userId":"kc-alice","customerEmail":"a@x.com","customerName":"Alice",
                 "totalAmount":%s,"items":[{"productId":1,"productName":"Keyboard","quantity":1,"price":%s}],
                 "createdAt":"2026-11-14T10:00:00"}""".formatted(orderId, amount, amount));
    }

    private void orderCancelled(long orderId) {
        send("order.status", """
                {"orderId":%d,"userId":"kc-alice","status":"CANCELLED","previousStatus":"CONFIRMED",
                 "totalAmount":100.00,"reason":"Cancelled by customer","occurredAt":"2026-11-14T10:05:00"}"""
                .formatted(orderId));
    }

    private void send(String destination, String json) {
        input.send(MessageBuilder.withPayload(json.getBytes()).setHeader("contentType", "application/json").build(),
                destination);
    }

    private JsonNode receiveResult() throws Exception {
        Message<byte[]> msg = output.receive(2000, "payment.result");
        assertThat(msg).as("payment.result message").isNotNull();
        return mapper.readTree(msg.getPayload());
    }

    @Test
    void approvedPaymentPublishesCompleted() throws Exception {
        orderCreated(1, "100.00");

        JsonNode result = receiveResult();
        assertThat(result.get("orderId").asLong()).isEqualTo(1);
        assertThat(result.get("status").asText()).isEqualTo("COMPLETED");
        Payment payment = repository.findByOrderId(1L).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getTransactionRef()).startsWith("TXN-");
    }

    @Test
    void declinedPaymentPublishesFailedWithReason() throws Exception {
        orderCreated(2, "9999.00");

        JsonNode result = receiveResult();
        assertThat(result.get("status").asText()).isEqualTo("FAILED");
        assertThat(result.get("reason").asText()).contains("exceeds the card limit");
    }

    @Test
    void duplicateEventChargesOnlyOnce() throws Exception {
        orderCreated(3, "100.00");
        long first = receiveResult().get("paymentId").asLong();
        orderCreated(3, "100.00");
        long second = receiveResult().get("paymentId").asLong();

        assertThat(second).isEqualTo(first);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void cancellationAfterPaymentRefunds() throws Exception {
        orderCreated(4, "100.00");
        receiveResult();
        orderCancelled(4);

        assertThat(repository.findByOrderId(4L).orElseThrow().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void cancellationBeforePaymentPreventsTheCharge() {
        orderCancelled(5);
        orderCreated(5, "100.00");   // arrives late

        assertThat(output.receive(500, "payment.result")).isNull();
        assertThat(repository.findByOrderId(5L).orElseThrow().getStatus()).isEqualTo(PaymentStatus.SKIPPED);
    }

    @Test
    void customersSeeOnlyTheirOwnPayments() throws Exception {
        orderCreated(6, "100.00");
        receiveResult();

        mvc.perform(get("/api/payments/orders/6").header("X-User-ID", "kc-alice"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status", is("COMPLETED")));
        mvc.perform(get("/api/payments/orders/6").header("X-User-ID", "kc-bob"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/payments/admin").header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
