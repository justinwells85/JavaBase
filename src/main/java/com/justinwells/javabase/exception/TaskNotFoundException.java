package com.justinwells.javabase.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested task is not found.
 */
public class TaskNotFoundException extends RuntimeException {

    private final UUID taskId;

    public TaskNotFoundException(UUID taskId) {
        super("Task not found with id: " + taskId);
        this.taskId = taskId;
    }

    public UUID getTaskId() {
        return taskId;
    }
}
