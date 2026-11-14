package com.ecommerce.shop.notification;

/** Delivery channel (SMTP email or log). Public so tests can replace it. */
public interface NotificationSender {

    void send(Notification notification);
}
