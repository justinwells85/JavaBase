package com.justinwells.javabase.domain.event;

import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Task is updated.
 */
public class TaskUpdatedEvent extends BaseDomainEvent {

    private final String title;
    private final String description;
    private final TaskStatus status;
    private final String assignee;
    private final LocalDateTime dueDate;

    public TaskUpdatedEvent(Task task, UUID correlationId) {
        super(task.getId(), "Task", correlationId);
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.assignee = task.getAssignee();
        this.dueDate = task.getDueDate();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getAssignee() {
        return assignee;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }
}
