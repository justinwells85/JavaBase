package com.justinwells.javabase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.domain.event.TaskCreatedEvent;
import com.justinwells.javabase.domain.repository.OutboxEventRepository;
import com.justinwells.javabase.infrastructure.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisherService Tests")
class EventPublisherServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;

    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        eventPublisherService = new EventPublisherService(outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("should save event to outbox with correct fields")
    void shouldSaveEventToOutbox() throws JsonProcessingException {
        UUID taskId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();
        TaskCreatedEvent event = new TaskCreatedEvent(
            taskId,
            "Test Task",
            "testuser",
            correlationId
        );
        String expectedPayload = "{\"taskId\":\"" + taskId + "\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(expectedPayload);
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(i -> i.getArgument(0));

        eventPublisherService.publish(event);

        verify(outboxEventRepository).save(outboxEventCaptor.capture());
        OutboxEvent savedEvent = outboxEventCaptor.getValue();

        assertThat(savedEvent.getAggregateId()).isEqualTo(taskId.toString());
        assertThat(savedEvent.getAggregateType()).isEqualTo("Task");
        assertThat(savedEvent.getEventType()).isEqualTo("TaskCreated");
        assertThat(savedEvent.getPayload()).isEqualTo(expectedPayload);
        assertThat(savedEvent.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("should throw RuntimeException when serialization fails")
    void shouldThrowExceptionWhenSerializationFails() throws JsonProcessingException {
        UUID taskId = UUID.randomUUID();
        TaskCreatedEvent event = new TaskCreatedEvent(
            taskId,
            "Test Task",
            "testuser",
            UUID.randomUUID().toString()
        );

        when(objectMapper.writeValueAsString(event))
            .thenThrow(new JsonProcessingException("Serialization failed") {});

        assertThatThrownBy(() -> eventPublisherService.publish(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to serialize event");
    }
}
