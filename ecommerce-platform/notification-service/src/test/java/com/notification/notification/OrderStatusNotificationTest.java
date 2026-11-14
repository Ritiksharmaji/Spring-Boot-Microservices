package com.notification.notification;

import com.notification.notification.service.Notification;
import com.notification.notification.service.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Sends real JSON messages through the (in-memory) binder into the consumer. */
@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class OrderStatusNotificationTest {

    @Autowired InputDestination input;
    @MockitoBean NotificationSender sender;

    @BeforeEach
    void reset() {
        clearInvocations(sender);
    }

    private void publish(String status, String email) {
        String json = """
                {"orderId":42,"userId":"kc-alice","customerEmail":%s,"customerName":"Alice",
                 "status":"%s","previousStatus":"PENDING","totalAmount":1099.00,
                 "reason":"Payment failed: limit","occurredAt":"2026-11-14T10:00:00"}
                """.formatted(email == null ? "null" : "\"" + email + "\"", status);
        input.send(MessageBuilder.withPayload(json.getBytes()).setHeader("contentType", "application/json").build(),
                "order.status");
    }

    @Test
    void confirmedOrderSendsConfirmationEmail() {
        publish("CONFIRMED", "alice@example.com");

        ArgumentCaptor<Notification> sent = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo("alice@example.com");
        assertThat(sent.getValue().subject()).isEqualTo("Order #42 confirmed");
        assertThat(sent.getValue().body()).contains("1099.00");
    }

    @Test
    void cancelledOrderExplainsWhy() {
        publish("CANCELLED", "alice@example.com");

        ArgumentCaptor<Notification> sent = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(sent.capture());
        assertThat(sent.getValue().body()).contains("Payment failed: limit");
    }

    @Test
    void pendingOrMissingEmailSendsNothing() {
        publish("PENDING", "alice@example.com");
        publish("SHIPPED", null);
        verifyNoInteractions(sender);
    }
}
