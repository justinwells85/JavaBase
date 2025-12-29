package com.justinwells.javabase.domain.repository;

import com.justinwells.javabase.infrastructure.outbox.OutboxEvent;
import com.justinwells.javabase.infrastructure.outbox.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for OutboxEvent entity operations.
 *
 * <p>Used by the OutboxProcessor to reliably publish events to the message broker.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Finds pending events ordered by creation time for processing.
     *
     * @param status   the status to filter by (typically PENDING)
     * @param pageable pagination to limit batch size
     * @return list of events to process
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
        @Param("status") OutboxEventStatus status,
        Pageable pageable
    );

    /**
     * Updates the status of an event to PUBLISHED and sets the published timestamp.
     *
     * @param id          the event ID
     * @param status      the new status
     * @param publishedAt the publication timestamp
     */
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :status, e.publishedAt = :publishedAt "
        + "WHERE e.id = :id")
    void updateStatus(
        @Param("id") UUID id,
        @Param("status") OutboxEventStatus status,
        @Param("publishedAt") LocalDateTime publishedAt
    );

    /**
     * Deletes old published events for cleanup.
     *
     * @param status    the status to filter by (typically PUBLISHED)
     * @param threshold events published before this time will be deleted
     * @return number of deleted events
     */
    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.status = :status "
        + "AND e.publishedAt < :threshold")
    int deleteOldPublishedEvents(
        @Param("status") OutboxEventStatus status,
        @Param("threshold") LocalDateTime threshold
    );

    /**
     * Finds stuck events that have been pending for too long.
     *
     * @param status    the status to filter by
     * @param threshold events created before this time are considered stuck
     * @return list of stuck events
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status "
        + "AND e.createdAt < :threshold ORDER BY e.createdAt ASC")
    List<OutboxEvent> findStuckEvents(
        @Param("status") OutboxEventStatus status,
        @Param("threshold") LocalDateTime threshold
    );

    /**
     * Counts events by status for monitoring.
     *
     * @param status the status to count
     * @return count of events with the given status
     */
    long countByStatus(OutboxEventStatus status);
}
