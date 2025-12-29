package com.justinwells.javabase.infrastructure.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity for storing idempotency keys and their associated responses.
 *
 * <p>Used to ensure that duplicate requests with the same idempotency key
 * return the same response without re-executing the operation.
 */
@Entity
@Table(
    name = "idempotency_keys",
    indexes = {
        @Index(name = "idx_idempotency_expires_at", columnList = "expires_at")
    }
)
public class IdempotencyKey {

    @Id
    @Column(name = "key", length = 255)
    private String key;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Default constructor required by JPA.
     */
    protected IdempotencyKey() {
    }

    /**
     * Creates a new idempotency key record.
     *
     * @param key            the idempotency key
     * @param responseStatus the HTTP status code of the response
     * @param responseBody   the response body
     * @param ttlHours       time-to-live in hours
     */
    public IdempotencyKey(String key, int responseStatus, String responseBody, int ttlHours) {
        this.key = key;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(ttlHours);
    }

    /**
     * Checks if this key has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Getters

    public String getKey() {
        return key;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
