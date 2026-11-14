package com.app_ecom.messaging;

import com.app_ecom.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Saga step: reacts to the payment outcome.
 * Binding "paymentResult-in-0" -> destination "payment.result", consumer group "order-service".
 * Failures are retried by the binder and then routed to the dead-letter queue.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentResultListener {

    private final OrderService orderService;

    @Bean
    public Consumer<PaymentResultEvent> paymentResult() {
        return event -> {
            log.info("Payment {} for order {}", event.status(), event.orderId());
            orderService.handlePaymentResult(event);
        };
    }
}
