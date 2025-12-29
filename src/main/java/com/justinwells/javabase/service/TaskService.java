package com.justinwells.javabase.service;

import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import com.justinwells.javabase.dto.v1.TaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Task operations.
 */
public interface TaskService {

    /**
     * Creates a new task.
     *
     * @param request the task creation request
     * @return the created task
     */
    Task createTask(TaskRequest request);

    /**
     * Gets a task by ID.
     *
     * @param id the task ID
     * @return the task
     * @throws com.justinwells.javabase.exception.TaskNotFoundException if task not found
     */
    Task getTask(UUID id);

    /**
     * Updates an existing task.
     *
     * @param id      the task ID
     * @param request the update request
     * @return the updated task
     * @throws com.justinwells.javabase.exception.TaskNotFoundException if task not found
     */
    Task updateTask(UUID id, TaskRequest request);

    /**
     * Soft deletes a task.
     *
     * @param id the task ID
     * @throws com.justinwells.javabase.exception.TaskNotFoundException if task not found
     */
    void deleteTask(UUID id);

    /**
     * Lists all non-deleted tasks with pagination.
     *
     * @param pageable pagination parameters
     * @return page of tasks
     */
    Page<Task> listTasks(Pageable pageable);

    /**
     * Lists tasks filtered by status.
     *
     * @param status   the status to filter by
     * @param pageable pagination parameters
     * @return page of matching tasks
     */
    Page<Task> listTasksByStatus(TaskStatus status, Pageable pageable);

    /**
     * Lists tasks filtered by assignee.
     *
     * @param assignee the assignee to filter by
     * @param pageable pagination parameters
     * @return page of matching tasks
     */
    Page<Task> listTasksByAssignee(String assignee, Pageable pageable);

    /**
     * Lists tasks filtered by status and assignee.
     *
     * @param status   the status to filter by
     * @param assignee the assignee to filter by
     * @param pageable pagination parameters
     * @return page of matching tasks
     */
    Page<Task> listTasksByStatusAndAssignee(TaskStatus status, String assignee, Pageable pageable);
}
