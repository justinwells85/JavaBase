package com.justinwells.javabase.infrastructure.correlation;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that extracts or generates a correlation ID for request tracing.
 *
 * <p>The correlation ID is used to trace requests across service boundaries
 * and is included in all log messages via MDC (Mapped Diagnostic Context).
 *
 * <p>If the incoming request has an X-Correlation-Id header, it will be used.
 * Otherwise, a new UUID is generated.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = extractOrGenerateCorrelationId(httpRequest);

        try {
            // Add to MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Add to response header for client visibility
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            chain.doFilter(request, response);
        } finally {
            // Always clean up MDC to prevent memory leaks in thread pools
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        return correlationId;
    }

    /**
     * Gets the current correlation ID from MDC.
     *
     * @return the current correlation ID, or null if not set
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }

    /**
     * Gets the current correlation ID as UUID.
     *
     * @return the current correlation ID as UUID, or a new UUID if not set
     */
    public static UUID getCurrentCorrelationIdAsUuid() {
        String correlationId = getCurrentCorrelationId();
        if (correlationId != null) {
            try {
                return UUID.fromString(correlationId);
            } catch (IllegalArgumentException e) {
                // If not a valid UUID, generate a new one
                return UUID.randomUUID();
            }
        }
        return UUID.randomUUID();
    }
}
