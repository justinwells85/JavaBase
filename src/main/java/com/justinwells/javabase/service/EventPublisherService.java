package com.justinwells.javabase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.domain.event.DomainEvent;
import com.justinwells.javabase.domain.repository.OutboxEventRepository;
import com.justinwells.javabase.infrastructure.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for publishing domain events via the outbox pattern.
 *
 * <p>Events are stored in the outbox table within the same transaction
 * as the business operation, ensuring atomic writes. The OutboxProcessor
 * then publishes these events to the message broker.
 */
@Service
public class EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public EventPublisherService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a domain event by storing it in the outbox.
     *
     * <p>This method must be called within an existing transaction to ensure
     * atomicity with the business operation.
     *
     * @param event the domain event to publish
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                payload,
                event.getCorrelationId()
            );

            outboxEventRepository.save(outboxEvent);

            log.debug("Published event {} of type {} for aggregate {}",
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}: {}", event.getEventId(), e.getMessage());
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
