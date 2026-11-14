package com.notification.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

/**
 * Picks the delivery channel at start-up: real email when an SMTP host is configured
 * (MAIL_HOST), otherwise log-only. Decided when the bean is created — not with
 * {@code @ConditionalOnBean}, which runs before Spring Boot's mail auto-configuration
 * has registered the JavaMailSender and would always choose the log sender.
 */
@Slf4j
@Configuration
public class NotificationSenderConfig {

    @Bean
    NotificationSender notificationSender(ObjectProvider<JavaMailSender> mailSender,
                                          @Value("${spring.mail.host:}") String mailHost,
                                          @Value("${app.notification.from}") String from) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender != null && StringUtils.hasText(mailHost)) {
            log.info("Notifications will be emailed via SMTP host {} from {}", mailHost, from);
            return notification -> {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(notification.to());
                message.setSubject(notification.subject());
                message.setText(notification.body());
                sender.send(message);
                log.info("Email '{}' sent to {}", notification.subject(), notification.to());
            };
        }
        log.info("SMTP not configured (MAIL_HOST empty): notifications will only be logged");
        return n -> log.info("[notification] to={} subject='{}'\n{}", n.to(), n.subject(), n.body());
    }
}
