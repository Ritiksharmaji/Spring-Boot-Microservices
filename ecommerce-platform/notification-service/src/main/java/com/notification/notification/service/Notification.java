package com.notification.notification.service;

/** A rendered message ready to be delivered. */
public record Notification(String to, String subject, String body) {
}
