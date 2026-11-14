package com.notification.notification.messaging;

import com.notification.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/** orderStatus-in-0 <- "order.status" (group notification-service, retries + DLQ). */
@Configuration
@RequiredArgsConstructor
public class OrderStatusListener {

    private final NotificationService notificationService;

    @Bean
    public Consumer<OrderStatusChangedEvent> orderStatus() {
        return notificationService::notifyStatusChange;
    }
}
