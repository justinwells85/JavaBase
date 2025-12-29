package com.justinwells.javabase.controller.v1;

import com.justinwells.javabase.controller.VersionedController;
import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import com.justinwells.javabase.dto.v1.TaskRequest;
import com.justinwells.javabase.dto.v1.TaskResponse;
import com.justinwells.javabase.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for Task operations (API v1).
 */
@RestController
@RequestMapping(VersionedController.API_V1 + "/tasks")
@Tag(name = "Tasks", description = "Task management API")
public class TaskController extends VersionedController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "409", description = "Idempotency conflict")
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @Parameter(description = "Idempotency key for duplicate request detection")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Task task = taskService.createTask(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(TaskResponse.fromEntity(task));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "Task ID")
            @PathVariable UUID id) {

        Task task = taskService.getTask(id);
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID")
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request,
            @Parameter(description = "Idempotency key for duplicate request detection")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Task task = taskService.updateTask(id, request);
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID")
            @PathVariable UUID id,
            @Parameter(description = "Idempotency key for duplicate request detection")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List tasks with optional filtering")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
            content = @Content(schema = @Schema(implementation = TaskListResponse.class)))
    })
    public ResponseEntity<Page<TaskResponse>> listTasks(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by assignee")
            @RequestParam(required = false) String assignee,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Task> tasks;

        if (status != null && assignee != null) {
            tasks = taskService.listTasksByStatusAndAssignee(status, assignee, pageable);
        } else if (status != null) {
            tasks = taskService.listTasksByStatus(status, pageable);
        } else if (assignee != null) {
            tasks = taskService.listTasksByAssignee(assignee, pageable);
        } else {
            tasks = taskService.listTasks(pageable);
        }

        Page<TaskResponse> response = tasks.map(TaskResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * Schema class for OpenAPI documentation of paginated task list.
     */
    private static class TaskListResponse {
        public java.util.List<TaskResponse> content;
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;
    }
}
