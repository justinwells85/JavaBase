package com.justinwells.javabase.dto.v1;

import com.justinwells.javabase.domain.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for creating or updating a Task.
 */
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private TaskStatus status;

    @Size(max = 255, message = "Assignee must not exceed 255 characters")
    private String assignee;

    private LocalDateTime dueDate;

    public TaskRequest() {
    }

    public TaskRequest(String title, String description, TaskStatus status,
                       String assignee, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignee = assignee;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
