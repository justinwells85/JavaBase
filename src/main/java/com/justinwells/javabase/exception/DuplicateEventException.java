package com.justinwells.javabase.exception;

import java.util.UUID;

/**
 * Exception thrown when a duplicate event is detected.
 */
public class DuplicateEventException extends RuntimeException {

    private final UUID eventId;

    public DuplicateEventException(UUID eventId) {
        super("Duplicate event detected with id: " + eventId);
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}
