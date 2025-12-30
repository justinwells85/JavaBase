package com.justinwells.javabase.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.infrastructure.idempotency.ProcessedEvent;
import com.justinwells.javabase.infrastructure.idempotency.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Consumer for processing incoming task events.
 *
 * <p>This is an example consumer demonstrating idempotent event handling.
 * Events that have already been processed are silently ignored.
 */
@Component
public class TaskEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskEventConsumer.class);

    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public TaskEventConsumer(
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Handles incoming task events.
     *
     * @param message the incoming message
     */
    @Transactional
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void handleEvent(Message<String> message) {
        String eventId = (String) message.getHeaders().get("eventId");
        String eventType = (String) message.getHeaders().get("eventType");
        String correlationId = (String) message.getHeaders().get("correlationId");

        // Set correlation ID in MDC for logging
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }

        try {
            log.info("Received event: {} of type {}", eventId, eventType);

            // Check for duplicate event
            if (eventId != null) {
                UUID eventUuid = UUID.fromString(eventId);
                if (processedEventRepository.existsById(eventUuid)) {
                    log.info("Duplicate event ignored: {}", eventId);
                    return;
                }

                // Process the event
                processEvent(eventType, message.getPayload());

                // Mark event as processed
                processedEventRepository.save(new ProcessedEvent(eventUuid));
            } else {
                // No event ID - process without idempotency check
                processEvent(eventType, message.getPayload());
            }

        } catch (Exception e) {
            log.error("Failed to process event {}: {}", eventId, e.getMessage(), e);
            throw e; // Rethrow to trigger retry/DLQ handling
        } finally {
            MDC.remove("correlationId");
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void processEvent(String eventType, String payload) {
        try {
            JsonNode eventData = objectMapper.readTree(payload);

            switch (eventType) {
                case "TaskCreatedEvent":
                    handleTaskCreated(eventData);
                    break;
                case "TaskUpdatedEvent":
                    handleTaskUpdated(eventData);
                    break;
                case "TaskCompletedEvent":
                    handleTaskCompleted(eventData);
                    break;
                case "TaskDeletedEvent":
                    handleTaskDeleted(eventData);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to parse event payload: {}", e.getMessage());
            throw new RuntimeException("Failed to parse event payload", e);
        }
    }

    private void handleTaskCreated(JsonNode eventData) {
        String taskId = eventData.path("aggregateId").asText();
        String title = eventData.path("title").asText();
        log.info("Processing TaskCreatedEvent: taskId={}, title={}", taskId, title);
        // Add your business logic here
    }

    private void handleTaskUpdated(JsonNode eventData) {
        String taskId = eventData.path("aggregateId").asText();
        log.info("Processing TaskUpdatedEvent: taskId={}", taskId);
        // Add your business logic here
    }

    private void handleTaskCompleted(JsonNode eventData) {
        String taskId = eventData.path("aggregateId").asText();
        String title = eventData.path("title").asText();
        log.info("Processing TaskCompletedEvent: taskId={}, title={}", taskId, title);
        // Add your business logic here (e.g., send notification, update metrics)
    }

    private void handleTaskDeleted(JsonNode eventData) {
        String taskId = eventData.path("aggregateId").asText();
        log.info("Processing TaskDeletedEvent: taskId={}", taskId);
        // Add your business logic here
    }
}
