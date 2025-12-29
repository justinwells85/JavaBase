package com.justinwells.javabase.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base implementation for domain events providing common fields and behavior.
 */
public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final String eventVersion;
    private final UUID correlationId;
    private final LocalDateTime timestamp;
    private final UUID aggregateId;
    private final String aggregateType;

    protected BaseDomainEvent(UUID aggregateId, String aggregateType, UUID correlationId) {
        this.eventId = UUID.randomUUID();
        this.eventVersion = "1.0";
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getEventVersion() {
        return eventVersion;
    }

    @Override
    public UUID getCorrelationId() {
        return correlationId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public UUID getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }
}
