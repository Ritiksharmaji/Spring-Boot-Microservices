package com.app_ecom.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Stores an event in the outbox. Must be called inside the business transaction. */
@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void write(String aggregateId, String eventType, String binding, Object event) {
        OutboxEvent row = new OutboxEvent();
        row.setAggregateId(aggregateId);
        row.setEventType(eventType);
        row.setBinding(binding);
        try {
            row.setPayload(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize " + eventType, e);
        }
        repository.save(row);
    }
}
