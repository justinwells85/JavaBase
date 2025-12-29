package com.justinwells.javabase.domain.event;

import com.justinwells.javabase.domain.model.Task;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Task is completed.
 */
public class TaskCompletedEvent extends BaseDomainEvent {

    private final String title;
    private final String assignee;
    private final LocalDateTime completedAt;

    public TaskCompletedEvent(Task task, UUID correlationId) {
        super(task.getId(), "Task", correlationId);
        this.title = task.getTitle();
        this.assignee = task.getAssignee();
        this.completedAt = task.getUpdatedAt();
    }

    public String getTitle() {
        return title;
    }

    public String getAssignee() {
        return assignee;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
