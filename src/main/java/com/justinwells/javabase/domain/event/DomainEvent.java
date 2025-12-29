package com.justinwells.javabase.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events.
 *
 * <p>All domain events must implement this interface to ensure
 * consistent event structure and traceability.
 */
public interface DomainEvent {

    /**
     * Gets the unique identifier for this event instance.
     *
     * @return the event ID
     */
    UUID getEventId();

    /**
     * Gets the type name of this event (e.g., "TaskCreatedEvent").
     *
     * @return the event type
     */
    String getEventType();

    /**
     * Gets the version of this event schema.
     *
     * @return the event version
     */
    String getEventVersion();

    /**
     * Gets the correlation ID for tracing this event across services.
     *
     * @return the correlation ID
     */
    UUID getCorrelationId();

    /**
     * Gets the timestamp when this event was created.
     *
     * @return the event timestamp
     */
    LocalDateTime getTimestamp();

    /**
     * Gets the ID of the aggregate that generated this event.
     *
     * @return the aggregate ID
     */
    UUID getAggregateId();

    /**
     * Gets the type of the aggregate (e.g., "Task").
     *
     * @return the aggregate type
     */
    String getAggregateType();
}
