package com.ecommerce.shop.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Reliability of module events (Spring Modulith event publication registry):
 * every event is stored in the same transaction that publishes it; a listener that fails leaves
 * its publication incomplete. This job re-delivers incomplete publications (at-least-once,
 * listeners are idempotent) and purges completed ones after a week.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
class EventPublicationMaintenance {

    private final IncompleteEventPublications incomplete;
    private final CompletedEventPublications completed;

    @Scheduled(fixedDelayString = "${app.events.retry-interval:PT1M}", initialDelayString = "${app.events.retry-interval:PT1M}")
    void retryIncomplete() {
        incomplete.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1));
    }

    @Scheduled(cron = "${app.events.cleanup-cron:0 30 3 * * *}")
    void purgeCompleted() {
        completed.deletePublicationsOlderThan(Duration.ofDays(7));
        log.info("Purged completed event publications older than 7 days");
    }
}
