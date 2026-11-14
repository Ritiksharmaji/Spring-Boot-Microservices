package com.ecommerce.shop.notification.internal;

import com.ecommerce.shop.notification.Notification;
import com.ecommerce.shop.notification.NotificationSender;
import com.ecommerce.shop.ordering.OrderPlaced;
import com.ecommerce.shop.ordering.OrderStatusChanged;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderNotifications {

    private final NotificationSender sender;

    @ApplicationModuleListener
    void on(OrderPlaced e) {
        sender.send(new Notification(e.customerEmail(), "Order #" + e.orderId() + " received",
                "Hi " + e.customerName() + ",\n\nThanks for your order of " + e.totalAmount()
                        + ". We are processing the payment and will confirm shortly."));
    }

    @ApplicationModuleListener
    void on(OrderStatusChanged e) {
        String order = "#" + e.orderId();
        Notification n = switch (e.status()) {
            case CONFIRMED -> new Notification(e.customerEmail(), "Order " + order + " confirmed",
                    "Hi " + e.customerName() + ",\n\nPayment of " + e.totalAmount() + " received; your order is confirmed.");
            case SHIPPED -> new Notification(e.customerEmail(), "Order " + order + " has shipped",
                    "Hi " + e.customerName() + ",\n\nYour order " + order + " is on its way.");
            case DELIVERED -> new Notification(e.customerEmail(), "Order " + order + " delivered",
                    "Hi " + e.customerName() + ",\n\nYour order " + order + " was delivered. Enjoy!");
            case CANCELLED -> new Notification(e.customerEmail(), "Order " + order + " cancelled",
                    "Hi " + e.customerName() + ",\n\nYour order " + order + " was cancelled"
                            + (e.reason() == null ? "." : ": " + e.reason() + ".") + "\nAny amount charged will be refunded.");
            case PENDING -> null;
        };
        if (n != null) {
            sender.send(n);
        }
    }
}
