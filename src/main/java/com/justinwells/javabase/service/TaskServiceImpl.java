package com.justinwells.javabase.service;

import com.justinwells.javabase.domain.event.TaskCompletedEvent;
import com.justinwells.javabase.domain.event.TaskCreatedEvent;
import com.justinwells.javabase.domain.event.TaskDeletedEvent;
import com.justinwells.javabase.domain.event.TaskUpdatedEvent;
import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import com.justinwells.javabase.domain.repository.TaskRepository;
import com.justinwells.javabase.dto.v1.TaskRequest;
import com.justinwells.javabase.exception.TaskNotFoundException;
import com.justinwells.javabase.infrastructure.correlation.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of TaskService with event publishing and caching.
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);
    private static final String CACHE_NAME = "tasks";

    private final TaskRepository taskRepository;
    private final EventPublisherService eventPublisher;

    public TaskServiceImpl(TaskRepository taskRepository, EventPublisherService eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Task createTask(TaskRequest request) {
        Task task = new Task(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignee(request.getAssignee());
        task.setDueDate(request.getDueDate());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task savedTask = taskRepository.save(task);

        UUID correlationId = CorrelationIdFilter.getCurrentCorrelationIdAsUuid();
        eventPublisher.publish(new TaskCreatedEvent(savedTask, correlationId));

        log.info("Created task with id: {}", savedTask.getId());

        return savedTask;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "#id")
    public Task getTask(UUID id) {
        return taskRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public Task updateTask(UUID id, TaskRequest request) {
        Task task = taskRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus previousStatus = task.getStatus();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getAssignee() != null) {
            task.setAssignee(request.getAssignee());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        Task updatedTask = taskRepository.save(task);

        UUID correlationId = CorrelationIdFilter.getCurrentCorrelationIdAsUuid();

        // Publish appropriate event based on status change
        if (updatedTask.getStatus() == TaskStatus.COMPLETED
                && previousStatus != TaskStatus.COMPLETED) {
            eventPublisher.publish(new TaskCompletedEvent(updatedTask, correlationId));
        } else {
            eventPublisher.publish(new TaskUpdatedEvent(updatedTask, correlationId));
        }

        log.info("Updated task with id: {}", updatedTask.getId());

        return updatedTask;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void deleteTask(UUID id) {
        Task task = taskRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        task.markAsDeleted();
        taskRepository.save(task);

        UUID correlationId = CorrelationIdFilter.getCurrentCorrelationIdAsUuid();
        eventPublisher.publish(new TaskDeletedEvent(task, correlationId));

        log.info("Soft deleted task with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasks(Pageable pageable) {
        return taskRepository.findAllNotDeleted(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasksByAssignee(String assignee, Pageable pageable) {
        return taskRepository.findByAssignee(assignee, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasksByStatusAndAssignee(
            TaskStatus status, String assignee, Pageable pageable) {
        return taskRepository.findByStatusAndAssignee(status, assignee, pageable);
    }
}
