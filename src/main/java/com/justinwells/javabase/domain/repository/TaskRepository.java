package com.justinwells.javabase.domain.repository;

import com.justinwells.javabase.domain.model.Task;
import com.justinwells.javabase.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Task entity operations.
 *
 * <p>All queries exclude soft-deleted tasks by default unless explicitly
 * stated otherwise.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Finds a task by ID, excluding soft-deleted tasks.
     *
     * @param id the task ID
     * @return the task if found and not deleted
     */
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Task> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Finds all tasks that are not soft-deleted.
     *
     * @param pageable pagination information
     * @return page of non-deleted tasks
     */
    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL")
    Page<Task> findAllNotDeleted(Pageable pageable);

    /**
     * Finds tasks by status, excluding soft-deleted tasks.
     *
     * @param status   the task status to filter by
     * @param pageable pagination information
     * @return page of matching tasks
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.deletedAt IS NULL")
    Page<Task> findByStatus(@Param("status") TaskStatus status, Pageable pageable);

    /**
     * Finds tasks by assignee, excluding soft-deleted tasks.
     *
     * @param assignee the assignee to filter by
     * @param pageable pagination information
     * @return page of matching tasks
     */
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee AND t.deletedAt IS NULL")
    Page<Task> findByAssignee(@Param("assignee") String assignee, Pageable pageable);

    /**
     * Finds tasks by status and assignee, excluding soft-deleted tasks.
     *
     * @param status   the task status to filter by
     * @param assignee the assignee to filter by
     * @param pageable pagination information
     * @return page of matching tasks
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status "
        + "AND t.assignee = :assignee AND t.deletedAt IS NULL")
    Page<Task> findByStatusAndAssignee(
        @Param("status") TaskStatus status,
        @Param("assignee") String assignee,
        Pageable pageable
    );

    /**
     * Counts tasks by status, excluding soft-deleted tasks.
     *
     * @param status the task status to count
     * @return count of matching tasks
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TaskStatus status);

    /**
     * Finds all tasks assigned to a specific user that are overdue.
     *
     * @param assignee the assignee to filter by
     * @return list of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee "
        + "AND t.dueDate < CURRENT_TIMESTAMP "
        + "AND t.status NOT IN ('COMPLETED', 'CANCELLED') "
        + "AND t.deletedAt IS NULL")
    List<Task> findOverdueTasksByAssignee(@Param("assignee") String assignee);

    /**
     * Checks if a task with the given ID exists and is not deleted.
     *
     * @param id the task ID
     * @return true if the task exists and is not deleted
     */
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.id = :id AND t.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") UUID id);
}
