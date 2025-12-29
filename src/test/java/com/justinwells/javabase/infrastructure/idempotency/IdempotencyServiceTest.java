package com.justinwells.javabase.infrastructure.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyService Tests")
class IdempotencyServiceTest {

    @Mock
    private IdempotencyKeyRepository repository;

    @Captor
    private ArgumentCaptor<IdempotencyKey> keyCaptor;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(repository, 24);
    }

    @Nested
    @DisplayName("findValidKey")
    class FindValidKeyTests {

        @Test
        @DisplayName("should return key when found and not expired")
        void shouldReturnKeyWhenFoundAndNotExpired() {
            String key = "test-key-123";
            IdempotencyKey storedKey = new IdempotencyKey(key, 200, "{\"id\":1}", 24);

            when(repository.findById(key)).thenReturn(Optional.of(storedKey));

            Optional<IdempotencyKey> result = idempotencyService.findValidKey(key);

            assertThat(result).isPresent();
            assertThat(result.get().getKey()).isEqualTo(key);
            assertThat(result.get().getResponseStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("should return empty when key not found")
        void shouldReturnEmptyWhenKeyNotFound() {
            String key = "nonexistent-key";

            when(repository.findById(key)).thenReturn(Optional.empty());

            Optional<IdempotencyKey> result = idempotencyService.findValidKey(key);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("storeKey")
    class StoreKeyTests {

        @Test
        @DisplayName("should store key with correct fields")
        void shouldStoreKeyWithCorrectFields() {
            String key = "new-key-456";
            int status = 201;
            String body = "{\"id\":\"abc\",\"name\":\"test\"}";

            when(repository.save(any(IdempotencyKey.class))).thenAnswer(i -> i.getArgument(0));

            idempotencyService.storeKey(key, status, body);

            verify(repository).save(keyCaptor.capture());
            IdempotencyKey savedKey = keyCaptor.getValue();

            assertThat(savedKey.getKey()).isEqualTo(key);
            assertThat(savedKey.getResponseStatus()).isEqualTo(status);
            assertThat(savedKey.getResponseBody()).isEqualTo(body);
            assertThat(savedKey.getExpiresAt()).isAfter(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("keyExists")
    class KeyExistsTests {

        @Test
        @DisplayName("should return true when valid key exists")
        void shouldReturnTrueWhenValidKeyExists() {
            String key = "existing-key";
            IdempotencyKey storedKey = new IdempotencyKey(key, 200, "{}", 24);

            when(repository.findById(key)).thenReturn(Optional.of(storedKey));

            boolean exists = idempotencyService.keyExists(key);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when key does not exist")
        void shouldReturnFalseWhenKeyDoesNotExist() {
            String key = "missing-key";

            when(repository.findById(key)).thenReturn(Optional.empty());

            boolean exists = idempotencyService.keyExists(key);

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("cleanupExpiredKeys")
    class CleanupExpiredKeysTests {

        @Test
        @DisplayName("should call repository to delete expired keys")
        void shouldCallRepositoryToDeleteExpiredKeys() {
            when(repository.deleteExpiredKeys(any(LocalDateTime.class))).thenReturn(5);

            idempotencyService.cleanupExpiredKeys();

            verify(repository).deleteExpiredKeys(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should handle zero expired keys gracefully")
        void shouldHandleZeroExpiredKeysGracefully() {
            when(repository.deleteExpiredKeys(any(LocalDateTime.class))).thenReturn(0);

            idempotencyService.cleanupExpiredKeys();

            verify(repository).deleteExpiredKeys(any(LocalDateTime.class));
        }
    }
}
