package com.notification.notification.service;

/** Delivery channel (email today; SMS / push could be added as further implementations). */
public interface NotificationSender {

    void send(Notification notification);
}
