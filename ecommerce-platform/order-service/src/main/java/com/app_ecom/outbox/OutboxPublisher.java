package com.app_ecom.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Polls the outbox and publishes pending events in order (at-least-once delivery;
 * consumers are idempotent). Published rows are purged after the retention period.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final StreamBridge streamBridge;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    @Value("${app.outbox.retention-days:7}")
    private int retentionDays;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : repository.findUnpublished(PageRequest.of(0, batchSize))) {
            event.setAttempts(event.getAttempts() + 1);
            try {
                Message<byte[]> message = MessageBuilder
                        .withPayload(event.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .setHeader("eventType", event.getEventType())
                        .setHeader("aggregateId", event.getAggregateId())
                        .build();
                if (!streamBridge.send(event.getBinding(), message)) {
                    throw new IllegalStateException("Broker did not accept the message");
                }
                event.setPublishedAt(LocalDateTime.now());
                event.setLastError(null);
                log.debug("Published {} for {}", event.getEventType(), event.getAggregateId());
            } catch (RuntimeException e) {
                event.setLastError(e.getMessage());
                log.warn("Publishing outbox event {} failed (attempt {}): {}",
                        event.getId(), event.getAttempts(), e.getMessage());
                break; // keep ordering; retry on the next poll
            }
        }
    }

    @Scheduled(cron = "${app.outbox.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void purgePublished() {
        int deleted = repository.deletePublishedBefore(LocalDateTime.now().minusDays(retentionDays));
        if (deleted > 0) {
            log.info("Purged {} published outbox events", deleted);
        }
    }
}
