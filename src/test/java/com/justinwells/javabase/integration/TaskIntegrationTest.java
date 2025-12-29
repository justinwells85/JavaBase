package com.justinwells.javabase.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justinwells.javabase.domain.model.TaskStatus;
import com.justinwells.javabase.domain.repository.TaskRepository;
import com.justinwells.javabase.dto.v1.TaskRequest;
import com.justinwells.javabase.dto.v1.TaskResponse;
import com.justinwells.javabase.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Task Integration Tests")
@Tag("integration")
class TaskIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-Id", UUID.randomUUID().toString());
        return headers;
    }

    @Nested
    @DisplayName("Task CRUD Operations")
    class TaskCrudTests {

        @Test
        @DisplayName("should create, read, update, and delete task")
        void shouldPerformCrudOperations() {
            // Create
            TaskRequest createRequest = new TaskRequest();
            createRequest.setTitle("Integration Test Task");
            createRequest.setDescription("Test Description");
            createRequest.setAssignee("integrationuser");
            createRequest.setDueDate(LocalDateTime.now().plusDays(7));

            HttpEntity<TaskRequest> createEntity = new HttpEntity<>(createRequest, createHeaders());

            ResponseEntity<TaskResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/tasks",
                createEntity,
                TaskResponse.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createResponse.getBody()).isNotNull();
            assertThat(createResponse.getBody().getTitle()).isEqualTo("Integration Test Task");
            assertThat(createResponse.getBody().getStatus()).isEqualTo(TaskStatus.PENDING);

            UUID taskId = createResponse.getBody().getId();

            // Read
            ResponseEntity<TaskResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/tasks/{id}",
                TaskResponse.class,
                taskId
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
            assertThat(getResponse.getBody().getId()).isEqualTo(taskId);

            // Update
            TaskRequest updateRequest = new TaskRequest();
            updateRequest.setTitle("Updated Integration Test Task");
            updateRequest.setStatus(TaskStatus.IN_PROGRESS);

            HttpEntity<TaskRequest> updateEntity = new HttpEntity<>(updateRequest, createHeaders());

            ResponseEntity<TaskResponse> updateResponse = restTemplate.exchange(
                "/api/v1/tasks/{id}",
                HttpMethod.PUT,
                updateEntity,
                TaskResponse.class,
                taskId
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody()).isNotNull();
            assertThat(updateResponse.getBody().getTitle()).isEqualTo("Updated Integration Test Task");
            assertThat(updateResponse.getBody().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

            // Delete
            HttpEntity<Void> deleteEntity = new HttpEntity<>(createHeaders());

            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/tasks/{id}",
                HttpMethod.DELETE,
                deleteEntity,
                Void.class,
                taskId
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deleted (should return 404)
            ResponseEntity<String> getAfterDeleteResponse = restTemplate.getForEntity(
                "/api/v1/tasks/{id}",
                String.class,
                taskId
            );

            assertThat(getAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("should return 404 for non-existent task")
        void shouldReturn404ForNonExistentTask() {
            UUID nonExistentId = UUID.randomUUID();

            ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/tasks/{id}",
                String.class,
                nonExistentId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Task Listing and Filtering")
    class TaskListingTests {

        @Test
        @DisplayName("should list all tasks with pagination")
        void shouldListAllTasksWithPagination() {
            // Create multiple tasks
            for (int i = 0; i < 5; i++) {
                TaskRequest request = new TaskRequest();
                request.setTitle("Task " + i);
                HttpEntity<TaskRequest> entity = new HttpEntity<>(request, createHeaders());
                restTemplate.postForEntity("/api/v1/tasks", entity, TaskResponse.class);
            }

            ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/tasks?page=0&size=3",
                String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("\"totalElements\":5");
            assertThat(response.getBody()).contains("\"size\":3");
        }

        @Test
        @DisplayName("should filter tasks by status")
        void shouldFilterTasksByStatus() {
            // Create tasks with different statuses
            TaskRequest pendingRequest = new TaskRequest();
            pendingRequest.setTitle("Pending Task");
            HttpEntity<TaskRequest> pendingEntity = new HttpEntity<>(pendingRequest, createHeaders());
            restTemplate.postForEntity("/api/v1/tasks", pendingEntity, TaskResponse.class);

            TaskRequest inProgressRequest = new TaskRequest();
            inProgressRequest.setTitle("In Progress Task");
            inProgressRequest.setStatus(TaskStatus.IN_PROGRESS);
            HttpEntity<TaskRequest> inProgressEntity = new HttpEntity<>(inProgressRequest, createHeaders());
            restTemplate.postForEntity("/api/v1/tasks", inProgressEntity, TaskResponse.class);

            ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/tasks?status=PENDING",
                String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Pending Task");
            assertThat(response.getBody()).doesNotContain("In Progress Task");
        }
    }

    @Nested
    @DisplayName("API Versioning")
    class ApiVersioningTests {

        @Test
        @DisplayName("should include API version header in responses")
        void shouldIncludeApiVersionHeader() {
            TaskRequest request = new TaskRequest();
            request.setTitle("Version Test Task");
            HttpEntity<TaskRequest> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<TaskResponse> response = restTemplate.postForEntity(
                "/api/v1/tasks",
                entity,
                TaskResponse.class
            );

            assertThat(response.getHeaders().get("API-Version")).contains("v1");
        }
    }

    @Nested
    @DisplayName("Correlation ID Tracking")
    class CorrelationIdTests {

        @Test
        @DisplayName("should echo correlation ID in response")
        void shouldEchoCorrelationId() {
            String correlationId = UUID.randomUUID().toString();

            HttpHeaders headers = createHeaders();
            headers.set("X-Correlation-Id", correlationId);

            TaskRequest request = new TaskRequest();
            request.setTitle("Correlation Test Task");
            HttpEntity<TaskRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TaskResponse> response = restTemplate.postForEntity(
                "/api/v1/tasks",
                entity,
                TaskResponse.class
            );

            assertThat(response.getHeaders().get("X-Correlation-Id")).contains(correlationId);
        }

        @Test
        @DisplayName("should generate correlation ID when not provided")
        void shouldGenerateCorrelationIdWhenNotProvided() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            TaskRequest request = new TaskRequest();
            request.setTitle("No Correlation Test Task");
            HttpEntity<TaskRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TaskResponse> response = restTemplate.postForEntity(
                "/api/v1/tasks",
                entity,
                TaskResponse.class
            );

            assertThat(response.getHeaders().get("X-Correlation-Id")).isNotNull();
        }
    }
}
