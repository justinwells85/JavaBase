package com.justinwells.javabase.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Task domain entity representing a unit of work.
 *
 * <p>This entity demonstrates the core patterns used in the JavaBase template:
 * <ul>
 *   <li>UUID-based primary key for distributed systems</li>
 *   <li>Soft deletes via deletedAt field</li>
 *   <li>Audit trail with createdAt/updatedAt timestamps</li>
 *   <li>Optimistic locking via version field</li>
 * </ul>
 */
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_status", columnList = "status"),
        @Index(name = "idx_tasks_assignee", columnList = "assignee"),
        @Index(name = "idx_tasks_deleted_at", columnList = "deleted_at")
    }
)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskStatus status;

    @Column(length = 255)
    private String assignee;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    /**
     * Default constructor required by JPA.
     */
    protected Task() {
    }

    /**
     * Creates a new Task with required fields.
     *
     * @param title the task title
     */
    public Task(String title) {
        this.title = title;
        this.status = TaskStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = TaskStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this task as deleted (soft delete).
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.status = TaskStatus.CANCELLED;
    }

    /**
     * Checks if this task has been soft deleted.
     *
     * @return true if the task is deleted, false otherwise
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Task{"
            + "id=" + id
            + ", title='" + title + '\''
            + ", status=" + status
            + ", assignee='" + assignee + '\''
            + '}';
    }
}
