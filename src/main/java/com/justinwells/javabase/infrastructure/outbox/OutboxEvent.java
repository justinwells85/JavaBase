package com.justinwells.javabase.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox event entity for implementing the transactional outbox pattern.
 *
 * <p>Events are stored in this table as part of the same transaction that
 * modifies business data. A background processor then publishes these
 * events to the message broker, ensuring at-least-once delivery semantics.
 */
@Entity
@Table(
    name = "outbox_events",
    indexes = {
        @Index(name = "idx_outbox_status", columnList = "status"),
        @Index(name = "idx_outbox_created_at", columnList = "created_at")
    }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OutboxEventStatus status;

    /**
     * Default constructor required by JPA.
     */
    protected OutboxEvent() {
    }

    /**
     * Creates a new outbox event.
     *
     * @param aggregateId   the ID of the aggregate that generated this event
     * @param aggregateType the type of the aggregate (e.g., "Task")
     * @param eventType     the type of the event (e.g., "TaskCreatedEvent")
     * @param payload       the JSON payload of the event
     * @param correlationId the correlation ID for tracing
     */
    public OutboxEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String payload,
            UUID correlationId) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId = correlationId;
        this.status = OutboxEventStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OutboxEventStatus.PENDING;
        }
    }

    /**
     * Marks this event as published.
     */
    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    /**
     * Marks this event as failed.
     */
    public void markAsFailed() {
        this.status = OutboxEventStatus.FAILED;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "OutboxEvent{"
            + "id=" + id
            + ", aggregateId=" + aggregateId
            + ", aggregateType='" + aggregateType + '\''
            + ", eventType='" + eventType + '\''
            + ", status=" + status
            + '}';
    }
}
