package com.ecommerce.shop.notification.internal;

import com.ecommerce.shop.notification.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

/** SMTP when MAIL_HOST is set, otherwise log-only (decided at bean creation, not with @ConditionalOnBean). */
@Slf4j
@Configuration
class NotificationSenderConfig {

    @Bean
    NotificationSender notificationSender(ObjectProvider<JavaMailSender> mailSender,
                                          @Value("${spring.mail.host:}") String host,
                                          @Value("${app.notification.from}") String from) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender != null && StringUtils.hasText(host)) {
            log.info("Order emails via SMTP {} from {}", host, from);
            return n -> {
                SimpleMailMessage m = new SimpleMailMessage();
                m.setFrom(from);
                m.setTo(n.to());
                m.setSubject(n.subject());
                m.setText(n.body());
                sender.send(m);
            };
        }
        log.info("MAIL_HOST not set: order emails are only logged");
        return n -> log.info("[email] to={} subject='{}'", n.to(), n.subject());
    }
}
