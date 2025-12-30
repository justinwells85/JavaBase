package com.justinwells.javabase.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.domain.event.DomainEvent;
import com.justinwells.javabase.domain.event.TaskCreatedEvent;
import com.justinwells.javabase.domain.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskEventProducer Tests")
class TaskEventProducerTest {

    @Mock
    private StreamBridge streamBridge;

    @Mock
    private ObjectMapper objectMapper;

    private TaskEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new TaskEventProducer(streamBridge, objectMapper);
    }

    private DomainEvent createTestEvent() {
        Task task = new Task("Test Task");
        task.setId(UUID.randomUUID());
        return new TaskCreatedEvent(task, UUID.randomUUID());
    }

    @Nested
    @DisplayName("publish()")
    class PublishTests {

        @Test
        @DisplayName("should return true when message is sent successfully")
        void shouldReturnTrueWhenMessageSentSuccessfully() throws JsonProcessingException {
            DomainEvent event = createTestEvent();
            when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
            when(streamBridge.send(eq("taskEventsOut-out-0"), any(Message.class))).thenReturn(true);

            boolean result = producer.publish(event);

            assertThat(result).isTrue();
            verify(streamBridge).send(eq("taskEventsOut-out-0"), any(Message.class));
        }

        @Test
        @DisplayName("should return false when message send fails")
        void shouldReturnFalseWhenMessageSendFails() throws JsonProcessingException {
            DomainEvent event = createTestEvent();
            when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
            when(streamBridge.send(eq("taskEventsOut-out-0"), any(Message.class))).thenReturn(false);

            boolean result = producer.publish(event);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when serialization fails")
        void shouldReturnFalseWhenSerializationFails() throws JsonProcessingException {
            DomainEvent event = createTestEvent();
            when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Serialization error") {});

            boolean result = producer.publish(event);

            assertThat(result).isFalse();
            verify(streamBridge, never()).send(any(), any(Message.class));
        }

        @Test
        @DisplayName("should set correct message headers")
        @SuppressWarnings("unchecked")
        void shouldSetCorrectMessageHeaders() throws JsonProcessingException {
            DomainEvent event = createTestEvent();
            when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
            when(streamBridge.send(eq("taskEventsOut-out-0"), any(Message.class))).thenReturn(true);

            producer.publish(event);

            ArgumentCaptor<Message<String>> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(streamBridge).send(eq("taskEventsOut-out-0"), messageCaptor.capture());

            Message<String> capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getHeaders().get("eventId")).isEqualTo(event.getEventId().toString());
            assertThat(capturedMessage.getHeaders().get("eventType")).isEqualTo(event.getEventType());
            assertThat(capturedMessage.getHeaders().get("aggregateId"))
                .isEqualTo(event.getAggregateId().toString());
            assertThat(capturedMessage.getHeaders().get("aggregateType")).isEqualTo(event.getAggregateType());
            assertThat(capturedMessage.getHeaders().get("correlationId"))
                .isEqualTo(event.getCorrelationId().toString());
            assertThat(capturedMessage.getHeaders().get("routingKey")).isNotNull();
        }

        @Test
        @DisplayName("should build correct routing key for TaskCreatedEvent")
        @SuppressWarnings("unchecked")
        void shouldBuildCorrectRoutingKey() throws JsonProcessingException {
            DomainEvent event = createTestEvent();
            when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
            when(streamBridge.send(eq("taskEventsOut-out-0"), any(Message.class))).thenReturn(true);

            producer.publish(event);

            ArgumentCaptor<Message<String>> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(streamBridge).send(eq("taskEventsOut-out-0"), messageCaptor.capture());

            Message<String> capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getHeaders().get("routingKey")).isEqualTo("task.created");
        }
    }
}
