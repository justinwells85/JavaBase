package com.justinwells.javabase.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyFilter Tests")
class IdempotencyFilterTest {

    private IdempotencyFilter filter;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private FilterChain filterChain;

    private ObjectMapper objectMapper;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new IdempotencyFilter(idempotencyService, objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("Non-mutating methods")
    class NonMutatingMethodsTests {

        @Test
        @DisplayName("should pass through GET requests without idempotency check")
        void shouldPassThroughGetRequests() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/v1/tasks");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }

        @Test
        @DisplayName("should pass through HEAD requests without idempotency check")
        void shouldPassThroughHeadRequests() throws ServletException, IOException {
            request.setMethod("HEAD");
            request.setRequestURI("/api/v1/tasks");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }

        @Test
        @DisplayName("should pass through OPTIONS requests without idempotency check")
        void shouldPassThroughOptionsRequests() throws ServletException, IOException {
            request.setMethod("OPTIONS");
            request.setRequestURI("/api/v1/tasks");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }
    }

    @Nested
    @DisplayName("Actuator endpoints")
    class ActuatorEndpointsTests {

        @Test
        @DisplayName("should skip actuator endpoints")
        void shouldSkipActuatorEndpoints() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/actuator/health");
            request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, UUID.randomUUID().toString());

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }
    }

    @Nested
    @DisplayName("Missing idempotency key")
    class MissingIdempotencyKeyTests {

        @Test
        @DisplayName("should proceed without idempotency check when key is missing")
        void shouldProceedWithoutKeyWhenMissing() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/v1/tasks");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }

        @Test
        @DisplayName("should proceed without idempotency check when key is blank")
        void shouldProceedWithoutKeyWhenBlank() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/v1/tasks");
            request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, "   ");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }
    }

    @Nested
    @DisplayName("Cached response")
    class CachedResponseTests {

        @Test
        @DisplayName("should return cached response when key exists")
        void shouldReturnCachedResponseWhenKeyExists() throws ServletException, IOException {
            String idempotencyKey = UUID.randomUUID().toString();
            IdempotencyKey cachedKey = new IdempotencyKey();
            cachedKey.setIdempotencyKey(idempotencyKey);
            cachedKey.setResponseStatus(201);
            cachedKey.setResponseBody("{\"id\":\"123\"}");

            request.setMethod("POST");
            request.setRequestURI("/api/v1/tasks");
            request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, idempotencyKey);

            when(idempotencyService.findValidKey(idempotencyKey)).thenReturn(Optional.of(cachedKey));

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getContentAsString()).isEqualTo("{\"id\":\"123\"}");
            assertThat(response.getHeader("X-Idempotency-Replayed")).isEqualTo("true");
            verifyNoInteractions(filterChain);
        }
    }

    @Nested
    @DisplayName("New requests")
    class NewRequestsTests {

        @Test
        @DisplayName("should check for existing key on POST requests")
        void shouldCheckForExistingKeyOnPost() throws ServletException, IOException {
            String idempotencyKey = UUID.randomUUID().toString();
            request.setMethod("POST");
            request.setRequestURI("/api/v1/tasks");
            request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, idempotencyKey);

            when(idempotencyService.findValidKey(idempotencyKey)).thenReturn(Optional.empty());

            filter.doFilter(request, response, filterChain);

            verify(idempotencyService).findValidKey(idempotencyKey);
            verify(filterChain).doFilter(eq(request), any());
        }

        @Test
        @DisplayName("should check for existing key on PUT requests")
        void shouldCheckForExistingKeyOnPut() throws ServletException, IOException {
            String idempotencyKey = UUID.randomUUID().toString();
            request.setMethod("PUT");
            request.setRequestURI("/api/v1/tasks/123");
            request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, idempotencyKey);

            when(idempotencyService.findValidKey(idempotencyKey)).thenReturn(Optional.empty());

            filter.doFilter(request, response, filterChain);

            verify(idempotencyService).findValidKey(idempotencyKey);
        }

        @Test
        @DisplayName("should check for existing key on DELETE requests")
        void shouldCheckForExistingKeyOnDelete() throws ServletException, IOException {
            String idempotencyKey = UUID.randomUUID().toString();
            request.setMethod("DELETE");
            request.setRequestURI("/api/v1/tasks/123");
            request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, idempotencyKey);

            when(idempotencyService.findValidKey(idempotencyKey)).thenReturn(Optional.empty());

            filter.doFilter(request, response, filterChain);

            verify(idempotencyService).findValidKey(idempotencyKey);
        }
    }
}
