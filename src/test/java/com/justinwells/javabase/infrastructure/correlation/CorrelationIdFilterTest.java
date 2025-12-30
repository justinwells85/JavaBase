package com.justinwells.javabase.infrastructure.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorrelationIdFilter Tests")
class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @Nested
    @DisplayName("doFilter")
    class DoFilterTests {

        @Test
        @DisplayName("should use existing correlation ID from header")
        void shouldUseExistingCorrelationId() throws ServletException, IOException {
            String existingCorrelationId = UUID.randomUUID().toString();
            request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingCorrelationId);

            filter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isEqualTo(existingCorrelationId);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should generate new correlation ID when header is missing")
        void shouldGenerateNewCorrelationIdWhenMissing() throws ServletException, IOException {
            filter.doFilter(request, response, filterChain);

            String correlationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
            assertThat(correlationId).isNotNull();
            assertThat(UUID.fromString(correlationId)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should generate new correlation ID when header is blank")
        void shouldGenerateNewCorrelationIdWhenBlank() throws ServletException, IOException {
            request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");

            filter.doFilter(request, response, filterChain);

            String correlationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
            assertThat(correlationId).isNotBlank();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should clean up MDC after filter execution")
        void shouldCleanUpMdcAfterFilter() throws ServletException, IOException {
            filter.doFilter(request, response, filterChain);

            assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
        }
    }

    @Nested
    @DisplayName("Static helper methods")
    class StaticHelperMethodsTests {

        @Test
        @DisplayName("getCurrentCorrelationId should return null when not set")
        void getCurrentCorrelationIdShouldReturnNullWhenNotSet() {
            assertThat(CorrelationIdFilter.getCurrentCorrelationId()).isNull();
        }

        @Test
        @DisplayName("getCurrentCorrelationId should return value when set")
        void getCurrentCorrelationIdShouldReturnValueWhenSet() {
            String correlationId = UUID.randomUUID().toString();
            MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, correlationId);

            assertThat(CorrelationIdFilter.getCurrentCorrelationId()).isEqualTo(correlationId);

            MDC.clear();
        }

        @Test
        @DisplayName("getCurrentCorrelationIdAsUuid should return new UUID when not set")
        void getCurrentCorrelationIdAsUuidShouldReturnNewUuidWhenNotSet() {
            UUID result = CorrelationIdFilter.getCurrentCorrelationIdAsUuid();
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getCurrentCorrelationIdAsUuid should parse valid UUID")
        void getCurrentCorrelationIdAsUuidShouldParseValidUuid() {
            UUID expected = UUID.randomUUID();
            MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, expected.toString());

            UUID result = CorrelationIdFilter.getCurrentCorrelationIdAsUuid();
            assertThat(result).isEqualTo(expected);

            MDC.clear();
        }

        @Test
        @DisplayName("getCurrentCorrelationIdAsUuid should return new UUID for invalid UUID string")
        void getCurrentCorrelationIdAsUuidShouldReturnNewUuidForInvalidString() {
            MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "not-a-valid-uuid");

            UUID result = CorrelationIdFilter.getCurrentCorrelationIdAsUuid();
            assertThat(result).isNotNull();

            MDC.clear();
        }
    }
}
