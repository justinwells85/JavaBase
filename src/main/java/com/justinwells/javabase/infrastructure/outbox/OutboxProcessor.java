package com.justinwells.javabase.infrastructure.outbox;

import com.justinwells.javabase.domain.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background processor that publishes outbox events to the message broker.
 *
 * <p>This component implements the transactional outbox pattern by:
 * <ul>
 *   <li>Polling the outbox table for pending events</li>
 *   <li>Publishing events to RabbitMQ via Spring Cloud Stream</li>
 *   <li>Marking events as published after successful delivery</li>
 *   <li>Cleaning up old published events</li>
 * </ul>
 *
 * <p>The processor can be disabled via configuration for testing.
 */
@Component
@ConditionalOnProperty(name = "outbox.polling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private static final String TASK_EVENTS_BINDING = "taskEventsOut-out-0";

    private final OutboxEventRepository outboxEventRepository;
    private final StreamBridge streamBridge;
    private final int batchSize;

    public OutboxProcessor(
            OutboxEventRepository outboxEventRepository,
            StreamBridge streamBridge,
            @Value("${outbox.polling.batch-size:100}") int batchSize) {
        this.outboxEventRepository = outboxEventRepository;
        this.streamBridge = streamBridge;
        this.batchSize = batchSize;
    }

    /**
     * Polls for pending events and publishes them to the message broker.
     */
    @Scheduled(fixedRateString = "${outbox.polling.interval-ms:1000}")
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
            OutboxEventStatus.PENDING,
            PageRequest.of(0, batchSize)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
                markAsPublished(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                event.markAsFailed();
                outboxEventRepository.save(event);
            }
        }
    }

    private void publishEvent(OutboxEvent event) {
        String routingKey = "task." + event.getEventType().toLowerCase()
            .replace("event", "")
            .replace("task", "");

        Message<String> message = MessageBuilder
            .withPayload(event.getPayload())
            .setHeader("eventId", event.getId().toString())
            .setHeader("eventType", event.getEventType())
            .setHeader("aggregateId", event.getAggregateId().toString())
            .setHeader("aggregateType", event.getAggregateType())
            .setHeader("correlationId", event.getCorrelationId().toString())
            .setHeader("routingKey", routingKey)
            .build();

        boolean sent = streamBridge.send(TASK_EVENTS_BINDING, message);

        if (!sent) {
            throw new RuntimeException("Failed to send message to " + TASK_EVENTS_BINDING);
        }

        log.debug("Published event {} of type {}", event.getId(), event.getEventType());
    }

    private void markAsPublished(OutboxEvent event) {
        outboxEventRepository.updateStatus(
            event.getId(),
            OutboxEventStatus.PUBLISHED,
            LocalDateTime.now()
        );
    }

    /**
     * Cleans up old published events to prevent table bloat.
     * Runs every hour by default.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupOldEvents() {
        // Keep published events for 7 days for debugging purposes
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        int deleted = outboxEventRepository.deleteOldPublishedEvents(
            OutboxEventStatus.PUBLISHED,
            threshold
        );

        if (deleted > 0) {
            log.info("Cleaned up {} old published outbox events", deleted);
        }
    }

    /**
     * Retries failed events.
     * Runs every 5 minutes by default.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void retryFailedEvents() {
        // Find events that failed more than 5 minutes ago
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

        List<OutboxEvent> failedEvents = outboxEventRepository.findStuckEvents(
            OutboxEventStatus.FAILED,
            threshold
        );

        if (failedEvents.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed outbox events", failedEvents.size());

        for (OutboxEvent event : failedEvents) {
            try {
                publishEvent(event);
                markAsPublished(event);
                log.info("Successfully retried event {}", event.getId());
            } catch (Exception e) {
                log.error("Retry failed for event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
