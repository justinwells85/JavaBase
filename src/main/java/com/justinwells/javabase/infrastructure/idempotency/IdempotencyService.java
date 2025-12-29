package com.justinwells.javabase.infrastructure.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing idempotency keys.
 *
 * <p>Provides functionality to store and retrieve idempotency keys,
 * ensuring that duplicate requests return cached responses.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyKeyRepository repository;
    private final int ttlHours;

    public IdempotencyService(
            IdempotencyKeyRepository repository,
            @Value("${idempotency.ttl-hours:24}") int ttlHours) {
        this.repository = repository;
        this.ttlHours = ttlHours;
    }

    /**
     * Checks if an idempotency key exists and is not expired.
     *
     * @param key the idempotency key
     * @return the stored response if key exists and is valid
     */
    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> findValidKey(String key) {
        return repository.findById(key)
            .filter(k -> !k.isExpired());
    }

    /**
     * Stores a new idempotency key with its response.
     *
     * @param key            the idempotency key
     * @param responseStatus the HTTP status code
     * @param responseBody   the response body (JSON)
     */
    @Transactional
    public void storeKey(String key, int responseStatus, String responseBody) {
        IdempotencyKey idempotencyKey = new IdempotencyKey(
            key,
            responseStatus,
            responseBody,
            ttlHours
        );
        repository.save(idempotencyKey);
        log.debug("Stored idempotency key: {}", key);
    }

    /**
     * Checks if an idempotency key already exists.
     *
     * @param key the idempotency key
     * @return true if key exists and is not expired
     */
    @Transactional(readOnly = true)
    public boolean keyExists(String key) {
        return findValidKey(key).isPresent();
    }

    /**
     * Cleans up expired idempotency keys.
     * Runs on a scheduled interval.
     */
    @Scheduled(fixedRateString = "${idempotency.cleanup.interval-ms:3600000}")
    @Transactional
    public void cleanupExpiredKeys() {
        int deleted = repository.deleteExpiredKeys(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired idempotency keys", deleted);
        }
    }
}
