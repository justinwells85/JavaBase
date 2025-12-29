package com.justinwells.javabase.infrastructure.outbox;

/**
 * Status of an outbox event in its publishing lifecycle.
 */
public enum OutboxEventStatus {

    /**
     * Event has been created but not yet published to the message broker.
     */
    PENDING,

    /**
     * Event has been successfully published to the message broker.
     */
    PUBLISHED,

    /**
     * Event failed to publish and requires attention.
     */
    FAILED
}
