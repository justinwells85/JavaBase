package com.justinwells.javabase.infrastructure.outbox;

import com.justinwells.javabase.domain.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxProcessor Tests")
class OutboxProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private StreamBridge streamBridge;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    private OutboxProcessor outboxProcessor;

    @BeforeEach
    void setUp() {
        outboxProcessor = new OutboxProcessor(outboxEventRepository, streamBridge, 100);
    }

    @Nested
    @DisplayName("processOutbox")
    class ProcessOutboxTests {

        @Test
        @DisplayName("should not process when no pending events")
        void shouldNotProcessWhenNoPendingEvents() {
            when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING),
                any(PageRequest.class)
            )).thenReturn(Collections.emptyList());

            outboxProcessor.processOutbox();

            verify(streamBridge, never()).send(anyString(), any(Message.class));
        }

        @Test
        @DisplayName("should publish pending events to message broker")
        void shouldPublishPendingEvents() {
            UUID eventId = UUID.randomUUID();
            OutboxEvent event = createTestEvent(eventId);

            when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING),
                any(PageRequest.class)
            )).thenReturn(List.of(event));
            when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

            outboxProcessor.processOutbox();

            verify(streamBridge).send(eq("taskEventsOut-out-0"), messageCaptor.capture());
            Message<String> sentMessage = messageCaptor.getValue();

            assertThat(sentMessage.getPayload()).isEqualTo("{\"taskId\":\"123\"}");
            assertThat(sentMessage.getHeaders().get("eventType")).isEqualTo("TaskCreated");
            assertThat(sentMessage.getHeaders().get("aggregateType")).isEqualTo("Task");
        }

        @Test
        @DisplayName("should mark event as published after successful send")
        void shouldMarkEventAsPublishedAfterSuccessfulSend() {
            UUID eventId = UUID.randomUUID();
            OutboxEvent event = createTestEvent(eventId);

            when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING),
                any(PageRequest.class)
            )).thenReturn(List.of(event));
            when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

            outboxProcessor.processOutbox();

            verify(outboxEventRepository).updateStatus(
                eq(eventId),
                eq(OutboxEventStatus.PUBLISHED),
                any(LocalDateTime.class)
            );
        }

        @Test
        @DisplayName("should mark event as failed when send fails")
        void shouldMarkEventAsFailedWhenSendFails() {
            UUID eventId = UUID.randomUUID();
            OutboxEvent event = createTestEvent(eventId);

            when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING),
                any(PageRequest.class)
            )).thenReturn(List.of(event));
            when(streamBridge.send(anyString(), any(Message.class))).thenReturn(false);

            outboxProcessor.processOutbox();

            verify(outboxEventRepository).save(event);
            assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("cleanupOldEvents")
    class CleanupOldEventsTests {

        @Test
        @DisplayName("should delete old published events")
        void shouldDeleteOldPublishedEvents() {
            when(outboxEventRepository.deleteOldPublishedEvents(
                eq(OutboxEventStatus.PUBLISHED),
                any(LocalDateTime.class)
            )).thenReturn(10);

            outboxProcessor.cleanupOldEvents();

            verify(outboxEventRepository).deleteOldPublishedEvents(
                eq(OutboxEventStatus.PUBLISHED),
                any(LocalDateTime.class)
            );
        }
    }

    @Nested
    @DisplayName("retryFailedEvents")
    class RetryFailedEventsTests {

        @Test
        @DisplayName("should not retry when no failed events")
        void shouldNotRetryWhenNoFailedEvents() {
            when(outboxEventRepository.findStuckEvents(
                eq(OutboxEventStatus.FAILED),
                any(LocalDateTime.class)
            )).thenReturn(Collections.emptyList());

            outboxProcessor.retryFailedEvents();

            verify(streamBridge, never()).send(anyString(), any(Message.class));
        }

        @Test
        @DisplayName("should retry failed events")
        void shouldRetryFailedEvents() {
            UUID eventId = UUID.randomUUID();
            OutboxEvent event = createTestEvent(eventId);
            event.markAsFailed();

            when(outboxEventRepository.findStuckEvents(
                eq(OutboxEventStatus.FAILED),
                any(LocalDateTime.class)
            )).thenReturn(List.of(event));
            when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

            outboxProcessor.retryFailedEvents();

            verify(streamBridge).send(eq("taskEventsOut-out-0"), any(Message.class));
            verify(outboxEventRepository).updateStatus(
                eq(eventId),
                eq(OutboxEventStatus.PUBLISHED),
                any(LocalDateTime.class)
            );
        }
    }

    private OutboxEvent createTestEvent(UUID eventId) {
        OutboxEvent event = new OutboxEvent(
            UUID.randomUUID(),
            "Task",
            "TaskCreated",
            "{\"taskId\":\"123\"}",
            UUID.randomUUID()
        );
        // Use reflection to set ID for testing
        try {
            java.lang.reflect.Field idField = OutboxEvent.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(event, eventId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return event;
    }
}
