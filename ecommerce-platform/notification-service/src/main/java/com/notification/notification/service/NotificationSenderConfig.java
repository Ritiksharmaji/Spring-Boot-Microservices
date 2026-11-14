package com.notification.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Picks the delivery channel: real email when SMTP is configured (spring.mail.host set,
 * which makes Spring Boot create a JavaMailSender), otherwise log-only.
 */
@Slf4j
@Configuration
public class NotificationSenderConfig {

    @Bean
    @ConditionalOnBean(JavaMailSender.class)
    NotificationSender emailSender(JavaMailSender mailSender, @Value("${app.notification.from}") String from) {
        log.info("Notifications will be sent by email from {}", from);
        return notification -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(notification.to());
            message.setSubject(notification.subject());
            message.setText(notification.body());
            mailSender.send(message);
            log.info("Email '{}' sent to {}", notification.subject(), notification.to());
        };
    }

    @Bean
    @ConditionalOnMissingBean(NotificationSender.class)
    NotificationSender logSender() {
        log.info("SMTP not configured: notifications will only be logged");
        return n -> log.info("[notification] to={} subject='{}'\n{}", n.to(), n.subject(), n.body());
    }
}
