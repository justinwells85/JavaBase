package com.justinwells.javabase.service;

import com.justinwells.javabase.domain.event.TaskCreatedEvent;
import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import com.justinwells.javabase.domain.repository.TaskRepository;
import com.justinwells.javabase.dto.v1.TaskRequest;
import com.justinwells.javabase.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EventPublisherService eventPublisher;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    private TaskRequest createTaskRequest;
    private Task existingTask;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        createTaskRequest = new TaskRequest();
        createTaskRequest.setTitle("Test Task");
        createTaskRequest.setDescription("Test Description");
        createTaskRequest.setAssignee("testuser");
        createTaskRequest.setDueDate(LocalDateTime.now().plusDays(7));

        existingTask = new Task("Existing Task");
        existingTask.setId(taskId);
        existingTask.setDescription("Existing Description");
        existingTask.setAssignee("existinguser");
        existingTask.setStatus(TaskStatus.PENDING);
    }

    @Nested
    @DisplayName("Create Task")
    class CreateTaskTests {

        @Test
        @DisplayName("should create task with all fields")
        void shouldCreateTaskWithAllFields() {
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(UUID.randomUUID());
                return task;
            });

            Task result = taskService.createTask(createTaskRequest);

            verify(taskRepository).save(taskCaptor.capture());
            Task savedTask = taskCaptor.getValue();

            assertThat(savedTask.getTitle()).isEqualTo("Test Task");
            assertThat(savedTask.getDescription()).isEqualTo("Test Description");
            assertThat(savedTask.getAssignee()).isEqualTo("testuser");
            assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("should publish TaskCreatedEvent")
        void shouldPublishTaskCreatedEvent() {
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(UUID.randomUUID());
                return task;
            });

            taskService.createTask(createTaskRequest);

            verify(eventPublisher).publish(any(TaskCreatedEvent.class));
        }

        @Test
        @DisplayName("should set default status to PENDING")
        void shouldSetDefaultStatusToPending() {
            createTaskRequest.setStatus(null);

            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(UUID.randomUUID());
                return task;
            });

            taskService.createTask(createTaskRequest);

            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Get Task")
    class GetTaskTests {

        @Test
        @DisplayName("should return task when found")
        void shouldReturnTaskWhenFound() {
            when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.of(existingTask));

            Task result = taskService.getTask(taskId);

            assertThat(result).isEqualTo(existingTask);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {
            when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTask(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());
        }
    }

    @Nested
    @DisplayName("Update Task")
    class UpdateTaskTests {

        @Test
        @DisplayName("should update task fields")
        void shouldUpdateTaskFields() {
            TaskRequest updateRequest = new TaskRequest();
            updateRequest.setTitle("Updated Title");
            updateRequest.setStatus(TaskStatus.IN_PROGRESS);

            when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            Task result = taskService.updateTask(taskId, updateRequest);

            assertThat(existingTask.getTitle()).isEqualTo("Updated Title");
            assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(taskId, createTaskRequest))
                .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Task")
    class DeleteTaskTests {

        @Test
        @DisplayName("should soft delete task")
        void shouldSoftDeleteTask() {
            when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            taskService.deleteTask(taskId);

            assertThat(existingTask.isDeleted()).isTrue();
            assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(TaskNotFoundException.class);

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("List Tasks")
    class ListTasksTests {

        @Test
        @DisplayName("should return paginated tasks")
        void shouldReturnPaginatedTasks() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> expectedPage = new PageImpl<>(List.of(existingTask));

            when(taskRepository.findAllNotDeleted(pageable)).thenReturn(expectedPage);

            Page<Task> result = taskService.listTasks(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(existingTask);
        }

        @Test
        @DisplayName("should filter by status")
        void shouldFilterByStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> expectedPage = new PageImpl<>(List.of(existingTask));

            when(taskRepository.findByStatus(TaskStatus.PENDING, pageable)).thenReturn(expectedPage);

            Page<Task> result = taskService.listTasksByStatus(TaskStatus.PENDING, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by assignee")
        void shouldFilterByAssignee() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> expectedPage = new PageImpl<>(List.of(existingTask));

            when(taskRepository.findByAssignee("testuser", pageable)).thenReturn(expectedPage);

            Page<Task> result = taskService.listTasksByAssignee("testuser", pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }
}
