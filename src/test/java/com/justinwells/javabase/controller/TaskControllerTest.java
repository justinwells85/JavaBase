package com.justinwells.javabase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.controller.v1.TaskController;
import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import com.justinwells.javabase.dto.v1.TaskRequest;
import com.justinwells.javabase.exception.TaskNotFoundException;
import com.justinwells.javabase.infrastructure.idempotency.IdempotencyService;
import com.justinwells.javabase.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaskController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.justinwells\\.javabase\\.infrastructure\\..*"
    )
)
@DisplayName("TaskController Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private IdempotencyService idempotencyService;

    private Task testTask;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        testTask = new Task("Test Task");
        testTask.setId(taskId);
        testTask.setDescription("Test Description");
        testTask.setAssignee("testuser");
        testTask.setStatus(TaskStatus.PENDING);
    }

    @Nested
    @DisplayName("POST /api/v1/tasks")
    class CreateTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should create task and return 201")
        void shouldCreateTaskAndReturn201() throws Exception {
            TaskRequest request = new TaskRequest();
            request.setTitle("New Task");
            request.setDescription("Description");

            when(taskService.createTask(any(TaskRequest.class))).thenReturn(testTask);

            mockMvc.perform(post("/api/v1/tasks")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(header().string("API-Version", "v1"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when title is missing")
        void shouldReturn400WhenTitleIsMissing() throws Exception {
            TaskRequest request = new TaskRequest();
            request.setDescription("Description without title");

            mockMvc.perform(post("/api/v1/tasks")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks/{id}")
    class GetTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should return task when found")
        void shouldReturnTaskWhenFound() throws Exception {
            when(taskService.getTask(taskId)).thenReturn(testTask);

            mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            when(taskService.getTask(taskId)).thenThrow(new TaskNotFoundException(taskId));

            mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: " + taskId));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/tasks/{id}")
    class UpdateTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should update task and return 200")
        void shouldUpdateTaskAndReturn200() throws Exception {
            TaskRequest request = new TaskRequest();
            request.setTitle("Updated Task");
            request.setStatus(TaskStatus.IN_PROGRESS);

            testTask.setTitle("Updated Task");
            testTask.setStatus(TaskStatus.IN_PROGRESS);

            when(taskService.updateTask(eq(taskId), any(TaskRequest.class))).thenReturn(testTask);

            mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            TaskRequest request = new TaskRequest();
            request.setTitle("Updated Task");

            when(taskService.updateTask(eq(taskId), any(TaskRequest.class)))
                .thenThrow(new TaskNotFoundException(taskId));

            mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tasks/{id}")
    class DeleteTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should delete task and return 204")
        void shouldDeleteTaskAndReturn204() throws Exception {
            doNothing().when(taskService).deleteTask(taskId);

            mockMvc.perform(delete("/api/v1/tasks/{id}", taskId)
                    .with(csrf()))
                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            doThrow(new TaskNotFoundException(taskId)).when(taskService).deleteTask(taskId);

            mockMvc.perform(delete("/api/v1/tasks/{id}", taskId)
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks")
    class ListTasksTests {

        @Test
        @WithMockUser
        @DisplayName("should return paginated tasks")
        void shouldReturnPaginatedTasks() throws Exception {
            when(taskService.listTasks(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testTask)));

            mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(taskId.toString()));
        }

        @Test
        @WithMockUser
        @DisplayName("should filter by status")
        void shouldFilterByStatus() throws Exception {
            when(taskService.listTasksByStatus(eq(TaskStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testTask)));

            mockMvc.perform(get("/api/v1/tasks")
                    .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @WithMockUser
        @DisplayName("should filter by assignee")
        void shouldFilterByAssignee() throws Exception {
            when(taskService.listTasksByAssignee(eq("testuser"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testTask)));

            mockMvc.perform(get("/api/v1/tasks")
                    .param("assignee", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        }
    }
}
