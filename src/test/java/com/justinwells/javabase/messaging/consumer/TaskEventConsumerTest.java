package com.justinwells.javabase.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.infrastructure.idempotency.ProcessedEvent;
import com.justinwells.javabase.infrastructure.idempotency.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskEventConsumer Tests")
class TaskEventConsumerTest {

    private TaskEventConsumer consumer;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new TaskEventConsumer(processedEventRepository, objectMapper);
    }

    @Nested
    @DisplayName("handleEvent")
    class HandleEventTests {

        @Test
        @DisplayName("should process new TaskCreatedEvent")
        void shouldProcessNewTaskCreatedEvent() {
            UUID eventId = UUID.randomUUID();
            String payload = "{\"aggregateId\":\"task-123\",\"title\":\"Test Task\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "TaskCreatedEvent")
                .setHeader("correlationId", UUID.randomUUID().toString())
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(false);

            consumer.handleEvent(message);

            ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
            verify(processedEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEventId()).isEqualTo(eventId);
        }

        @Test
        @DisplayName("should skip duplicate events")
        void shouldSkipDuplicateEvents() {
            UUID eventId = UUID.randomUUID();
            String payload = "{\"aggregateId\":\"task-123\",\"title\":\"Test Task\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "TaskCreatedEvent")
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(true);

            consumer.handleEvent(message);

            verify(processedEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should process TaskUpdatedEvent")
        void shouldProcessTaskUpdatedEvent() {
            UUID eventId = UUID.randomUUID();
            String payload = "{\"aggregateId\":\"task-123\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "TaskUpdatedEvent")
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(false);

            consumer.handleEvent(message);

            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        @DisplayName("should process TaskCompletedEvent")
        void shouldProcessTaskCompletedEvent() {
            UUID eventId = UUID.randomUUID();
            String payload = "{\"aggregateId\":\"task-123\",\"title\":\"Completed Task\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "TaskCompletedEvent")
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(false);

            consumer.handleEvent(message);

            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        @DisplayName("should process TaskDeletedEvent")
        void shouldProcessTaskDeletedEvent() {
            UUID eventId = UUID.randomUUID();
            String payload = "{\"aggregateId\":\"task-123\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "TaskDeletedEvent")
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(false);

            consumer.handleEvent(message);

            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        @DisplayName("should handle unknown event types gracefully")
        void shouldHandleUnknownEventTypes() {
            UUID eventId = UUID.randomUUID();
            String payload = "{\"aggregateId\":\"task-123\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "UnknownEventType")
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(false);

            consumer.handleEvent(message);

            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        @DisplayName("should process event without eventId")
        void shouldProcessEventWithoutEventId() {
            String payload = "{\"aggregateId\":\"task-123\",\"title\":\"Test Task\"}";

            Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("eventType", "TaskCreatedEvent")
                .build();

            consumer.handleEvent(message);

            verify(processedEventRepository, never()).existsById(any());
            verify(processedEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception for invalid JSON payload")
        void shouldThrowExceptionForInvalidPayload() {
            UUID eventId = UUID.randomUUID();
            String invalidPayload = "not valid json";

            Message<String> message = MessageBuilder.withPayload(invalidPayload)
                .setHeader("eventId", eventId.toString())
                .setHeader("eventType", "TaskCreatedEvent")
                .build();

            when(processedEventRepository.existsById(eventId)).thenReturn(false);

            assertThatThrownBy(() -> consumer.handleEvent(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse event payload");
        }
    }
}
