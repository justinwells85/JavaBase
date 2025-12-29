package com.justinwells.javabase.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Producer for publishing task events directly (alternative to outbox pattern).
 *
 * <p>Note: In most cases, events should be published via the EventPublisherService
 * which uses the outbox pattern for guaranteed delivery. This producer is available
 * for cases where at-most-once delivery is acceptable.
 */
@Component
public class TaskEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TaskEventProducer.class);

    private static final String TASK_EVENTS_BINDING = "taskEventsOut-out-0";

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public TaskEventProducer(StreamBridge streamBridge, ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a domain event directly to the message broker.
     *
     * <p>Warning: This method does not guarantee delivery. For reliable
     * event publishing, use EventPublisherService which implements the
     * outbox pattern.
     *
     * @param event the event to publish
     * @return true if the message was sent successfully
     */
    public boolean publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            String routingKey = buildRoutingKey(event);

            Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("eventId", event.getEventId().toString())
                .setHeader("eventType", event.getEventType())
                .setHeader("aggregateId", event.getAggregateId().toString())
                .setHeader("aggregateType", event.getAggregateType())
                .setHeader("correlationId", event.getCorrelationId().toString())
                .setHeader("routingKey", routingKey)
                .build();

            boolean sent = streamBridge.send(TASK_EVENTS_BINDING, message);

            if (sent) {
                log.debug("Published event {} of type {}", event.getEventId(), event.getEventType());
            } else {
                log.warn("Failed to publish event {} of type {}",
                    event.getEventId(), event.getEventType());
            }

            return sent;

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}: {}", event.getEventId(), e.getMessage());
            return false;
        }
    }

    private String buildRoutingKey(DomainEvent event) {
        // Convert TaskCreatedEvent -> task.created
        String eventType = event.getEventType()
            .replace("Event", "")
            .replace("Task", "task.");

        // Convert camelCase to lowercase
        return eventType.toLowerCase();
    }
}
