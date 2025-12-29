package com.justinwells.javabase.infrastructure.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking processed events to ensure idempotent event handling.
 *
 * <p>When an event is processed, its ID is stored here. Subsequent attempts
 * to process the same event will be silently ignored.
 */
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    /**
     * Default constructor required by JPA.
     */
    protected ProcessedEvent() {
    }

    /**
     * Creates a new processed event record.
     *
     * @param eventId the ID of the processed event
     */
    public ProcessedEvent(UUID eventId) {
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
