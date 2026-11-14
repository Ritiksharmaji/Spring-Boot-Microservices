package com.notification.notification.service;

import com.notification.notification.messaging.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Turns order lifecycle events into customer notifications. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSender sender;

    public void notifyStatusChange(OrderStatusChangedEvent event) {
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            log.warn("Order {} has no customer email; skipping notification", event.orderId());
            return;
        }
        Notification notification = render(event);
        if (notification != null) {
            sender.send(notification); // exceptions -> binder retry -> DLQ
        }
    }

    Notification render(OrderStatusChangedEvent e) {
        String name = e.customerName() == null ? "there" : e.customerName();
        String order = "#" + e.orderId();
        return switch (e.status()) {
            case "CONFIRMED" -> new Notification(e.customerEmail(), "Order " + order + " confirmed",
                    "Hi " + name + ",\n\nWe received your payment of " + e.totalAmount()
                            + " and your order " + order + " is confirmed.\nWe'll let you know when it ships.");
            case "SHIPPED" -> new Notification(e.customerEmail(), "Order " + order + " has shipped",
                    "Hi " + name + ",\n\nGood news: your order " + order + " is on its way.");
            case "DELIVERED" -> new Notification(e.customerEmail(), "Order " + order + " delivered",
                    "Hi " + name + ",\n\nYour order " + order + " was delivered. Enjoy!");
            case "CANCELLED" -> new Notification(e.customerEmail(), "Order " + order + " cancelled",
                    "Hi " + name + ",\n\nYour order " + order + " was cancelled"
                            + (e.reason() == null ? "." : ": " + e.reason() + ".")
                            + "\nAny amount charged will be refunded.");
            default -> null; // PENDING has no customer-facing message
        };
    }
}
