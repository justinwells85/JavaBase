package com.justinwells.javabase.domain.event;

import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain Event Tests")
class DomainEventTest {

    private Task task;
    private UUID correlationId;

    @BeforeEach
    void setUp() {
        task = new Task("Test Task");
        task.setId(UUID.randomUUID());
        task.setDescription("Test Description");
        task.setAssignee("john.doe");
        correlationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("TaskCreatedEvent")
    class TaskCreatedEventTests {

        @Test
        @DisplayName("should create event with correct properties")
        void shouldCreateEventWithCorrectProperties() {
            TaskCreatedEvent event = new TaskCreatedEvent(task, correlationId);

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventType()).isEqualTo("TaskCreatedEvent");
            assertThat(event.getEventVersion()).isEqualTo("1.0");
            assertThat(event.getCorrelationId()).isEqualTo(correlationId);
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getAggregateId()).isEqualTo(task.getId());
            assertThat(event.getAggregateType()).isEqualTo("Task");
        }

        @Test
        @DisplayName("should include task data")
        void shouldIncludeTaskData() {
            TaskCreatedEvent event = new TaskCreatedEvent(task, correlationId);

            assertThat(event.getTitle()).isEqualTo("Test Task");
            assertThat(event.getDescription()).isEqualTo("Test Description");
            assertThat(event.getAssignee()).isEqualTo("john.doe");
        }
    }

    @Nested
    @DisplayName("TaskUpdatedEvent")
    class TaskUpdatedEventTests {

        @Test
        @DisplayName("should create event with correct properties")
        void shouldCreateEventWithCorrectProperties() {
            task.setStatus(TaskStatus.IN_PROGRESS);
            TaskUpdatedEvent event = new TaskUpdatedEvent(task, correlationId);

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventType()).isEqualTo("TaskUpdatedEvent");
            assertThat(event.getAggregateId()).isEqualTo(task.getId());
            assertThat(event.getAggregateType()).isEqualTo("Task");
        }

        @Test
        @DisplayName("should include updated task data")
        void shouldIncludeUpdatedTaskData() {
            task.setTitle("Updated Title");
            task.setStatus(TaskStatus.IN_PROGRESS);
            TaskUpdatedEvent event = new TaskUpdatedEvent(task, correlationId);

            assertThat(event.getTitle()).isEqualTo("Updated Title");
            assertThat(event.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("TaskCompletedEvent")
    class TaskCompletedEventTests {

        @Test
        @DisplayName("should create event with correct properties")
        void shouldCreateEventWithCorrectProperties() {
            task.setStatus(TaskStatus.COMPLETED);
            TaskCompletedEvent event = new TaskCompletedEvent(task, correlationId);

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventType()).isEqualTo("TaskCompletedEvent");
            assertThat(event.getAggregateId()).isEqualTo(task.getId());
        }

        @Test
        @DisplayName("should include completed task data")
        void shouldIncludeCompletedTaskData() {
            task.setStatus(TaskStatus.COMPLETED);
            TaskCompletedEvent event = new TaskCompletedEvent(task, correlationId);

            assertThat(event.getTitle()).isEqualTo("Test Task");
            assertThat(event.getAssignee()).isEqualTo("john.doe");
        }
    }

    @Nested
    @DisplayName("TaskDeletedEvent")
    class TaskDeletedEventTests {

        @Test
        @DisplayName("should create event with correct properties")
        void shouldCreateEventWithCorrectProperties() {
            TaskDeletedEvent event = new TaskDeletedEvent(task, correlationId);

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventType()).isEqualTo("TaskDeletedEvent");
            assertThat(event.getAggregateId()).isEqualTo(task.getId());
            assertThat(event.getAggregateType()).isEqualTo("Task");
        }
    }
}
