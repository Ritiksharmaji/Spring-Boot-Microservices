package com.app_ecom.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Transactional outbox row: written in the same DB transaction as the order change,
 * published to the broker afterwards by {@link OutboxPublisher}. An event is therefore
 * never lost (broker down) and never sent for a change that was rolled back.
 */
@Entity
@Table(name = "outbox_event", indexes = @Index(name = "idx_outbox_unpublished", columnList = "publishedAt, id"))
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "order-42" */
    @Column(nullable = false)
    private String aggregateId;

    /** e.g. "OrderCreated" */
    @Column(nullable = false)
    private String eventType;

    /** Spring Cloud Stream output binding, e.g. "orderCreated-out-0" */
    @Column(nullable = false)
    private String binding;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime publishedAt;

    private int attempts;

    private String lastError;
}
