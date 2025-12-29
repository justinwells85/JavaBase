package com.justinwells.javabase.domain.event;

import com.justinwells.javabase.domain.model.Task;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Task is deleted (soft delete).
 */
public class TaskDeletedEvent extends BaseDomainEvent {

    private final String title;
    private final LocalDateTime deletedAt;

    public TaskDeletedEvent(Task task, UUID correlationId) {
        super(task.getId(), "Task", correlationId);
        this.title = task.getTitle();
        this.deletedAt = task.getDeletedAt();
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
