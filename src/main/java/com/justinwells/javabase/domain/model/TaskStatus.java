package com.justinwells.javabase.domain.model;

/**
 * Represents the possible states of a Task in its lifecycle.
 */
public enum TaskStatus {

    /**
     * Task has been created but work has not started.
     */
    PENDING,

    /**
     * Task is currently being worked on.
     */
    IN_PROGRESS,

    /**
     * Task has been completed successfully.
     */
    COMPLETED,

    /**
     * Task has been cancelled and will not be completed.
     */
    CANCELLED
}
