package com.ecommerce.payment.messaging;

import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Message handlers (Spring Cloud Stream functional style).
 * orderCreated-in-0 <- "order.created", orderStatus-in-0 <- "order.status".
 * Exceptions trigger binder retries and finally the dead-letter queue.
 */
@Configuration
@RequiredArgsConstructor
public class PaymentEventHandlers {

    private final PaymentService paymentService;

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return paymentService::processPayment;
    }

    @Bean
    public Consumer<OrderStatusChangedEvent> orderStatus() {
        return event -> {
            if ("CANCELLED".equals(event.status())) {
                paymentService.handleOrderCancelled(event.orderId(), event.userId(), event.totalAmount());
            }
        };
    }
}
