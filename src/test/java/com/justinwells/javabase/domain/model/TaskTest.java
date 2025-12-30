package com.justinwells.javabase.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Task Entity Tests")
class TaskTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create task with title and default PENDING status")
        void shouldCreateTaskWithTitleAndDefaultStatus() {
            Task task = new Task("Test Task");

            assertThat(task.getTitle()).isEqualTo("Test Task");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDeleteTests {

        @Test
        @DisplayName("should mark task as deleted")
        void shouldMarkTaskAsDeleted() {
            Task task = new Task("Test Task");

            task.markAsDeleted();

            assertThat(task.isDeleted()).isTrue();
            assertThat(task.getDeletedAt()).isNotNull();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        }

        @Test
        @DisplayName("isDeleted should return false for non-deleted task")
        void isDeletedShouldReturnFalseForNonDeletedTask() {
            Task task = new Task("Test Task");

            assertThat(task.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Lifecycle callbacks")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("onCreate should set timestamps and default status")
        void onCreateShouldSetTimestampsAndDefaultStatus() {
            Task task = new Task("Test Task");
            task.onCreate();

            assertThat(task.getCreatedAt()).isNotNull();
            assertThat(task.getUpdatedAt()).isNotNull();
            assertThat(task.getCreatedAt()).isEqualTo(task.getUpdatedAt());
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("onCreate should set status to PENDING if null")
        void onCreateShouldSetStatusToPendingIfNull() {
            Task task = new Task("Test Task");
            // Manually set status to null to test the null check
            task.setStatus(null);

            task.onCreate();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("onUpdate should update the updatedAt timestamp")
        void onUpdateShouldUpdateTimestamp() throws InterruptedException {
            Task task = new Task("Test Task");
            task.onCreate();
            LocalDateTime originalUpdatedAt = task.getUpdatedAt();

            // Small delay to ensure different timestamp
            Thread.sleep(10);
            task.onUpdate();

            assertThat(task.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSettersTests {

        @Test
        @DisplayName("should set and get id")
        void shouldSetAndGetId() {
            Task task = new Task("Test Task");
            UUID id = UUID.randomUUID();

            task.setId(id);

            assertThat(task.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("should set and get description")
        void shouldSetAndGetDescription() {
            Task task = new Task("Test Task");

            task.setDescription("Test Description");

            assertThat(task.getDescription()).isEqualTo("Test Description");
        }

        @Test
        @DisplayName("should set and get assignee")
        void shouldSetAndGetAssignee() {
            Task task = new Task("Test Task");

            task.setAssignee("john.doe");

            assertThat(task.getAssignee()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("should set and get dueDate")
        void shouldSetAndGetDueDate() {
            Task task = new Task("Test Task");
            LocalDateTime dueDate = LocalDateTime.now().plusDays(7);

            task.setDueDate(dueDate);

            assertThat(task.getDueDate()).isEqualTo(dueDate);
        }

        @Test
        @DisplayName("should set and get status")
        void shouldSetAndGetStatus() {
            Task task = new Task("Test Task");

            task.setStatus(TaskStatus.IN_PROGRESS);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            Task task = new Task("Test Task");
            task.setId(UUID.randomUUID());

            assertThat(task).isEqualTo(task);
        }

        @Test
        @DisplayName("should be equal when ids match")
        void shouldBeEqualWhenIdsMatch() {
            UUID id = UUID.randomUUID();
            Task task1 = new Task("Task 1");
            task1.setId(id);
            Task task2 = new Task("Task 2");
            task2.setId(id);

            assertThat(task1).isEqualTo(task2);
        }

        @Test
        @DisplayName("should not be equal when ids differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            Task task1 = new Task("Test Task");
            task1.setId(UUID.randomUUID());
            Task task2 = new Task("Test Task");
            task2.setId(UUID.randomUUID());

            assertThat(task1).isNotEqualTo(task2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            Task task = new Task("Test Task");
            task.setId(UUID.randomUUID());

            assertThat(task).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Task task = new Task("Test Task");
            task.setId(UUID.randomUUID());

            assertThat(task).isNotEqualTo("Not a task");
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            Task task = new Task("Test Task");

            int hashCode1 = task.hashCode();
            int hashCode2 = task.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should return string representation")
        void shouldReturnStringRepresentation() {
            Task task = new Task("Test Task");
            UUID id = UUID.randomUUID();
            task.setId(id);
            task.setAssignee("john.doe");

            String result = task.toString();

            assertThat(result).contains("Task{");
            assertThat(result).contains("id=" + id);
            assertThat(result).contains("title='Test Task'");
            assertThat(result).contains("status=PENDING");
            assertThat(result).contains("assignee='john.doe'");
        }
    }
}
