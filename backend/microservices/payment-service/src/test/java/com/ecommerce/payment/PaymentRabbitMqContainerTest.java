package com.ecommerce.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end through a real RabbitMQ broker (Testcontainers): an order.created message
 * published to the exchange is consumed, charged, and answered on payment.result
 * (queue payment.result.order-service, created by the producer's required-groups).
 * Skipped without Docker.
 */
@SpringBootTest(properties = {
        "spring.cloud.stream.default-binder=rabbit",
        "spring.datasource.url=jdbc:h2:mem:payments-rabbit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers(disabledWithoutDocker = true)
class PaymentRabbitMqContainerTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired ObjectMapper mapper;

    @Test
    void orderCreatedOnTheBrokerProducesPaymentResult() throws Exception {
        String json = """
                {"orderId":77,"userId":"kc-alice","customerEmail":"a@x.com","customerName":"Alice",
                 "totalAmount":250.00,"items":[],"createdAt":"2026-11-14T10:00:00"}""";
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        rabbitTemplate.send("order.created", "#", new Message(json.getBytes(StandardCharsets.UTF_8), props));

        // The result queue is declared by the binder on the first publish, so until then
        // RabbitMQ answers 404 NOT_FOUND: keep polling for up to ~10 seconds.
        Message reply = null;
        for (int i = 0; i < 50 && reply == null; i++) {
            try {
                reply = rabbitTemplate.receive("payment.result.order-service", 200);
            } catch (AmqpException queueNotDeclaredYet) {
                Thread.sleep(200);
            }
        }
        assertThat(reply).as("payment.result message").isNotNull();
        JsonNode result = mapper.readTree(reply.getBody());
        assertThat(result.get("orderId").asLong()).isEqualTo(77);
        assertThat(result.get("status").asText()).isEqualTo("COMPLETED");
    }
}
