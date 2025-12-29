package com.justinwells.javabase.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * Filter that enforces idempotency for mutating HTTP operations.
 *
 * <p>For POST, PUT, PATCH, and DELETE requests, this filter:
 * <ul>
 *   <li>Checks for an Idempotency-Key header</li>
 *   <li>Returns cached response if key was previously used</li>
 *   <li>Caches the response for new keys</li>
 * </ul>
 */
@Component
@Order(10)
public class IdempotencyFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private static final Set<String> IDEMPOTENT_METHODS = Set.of(
        HttpMethod.POST.name(),
        HttpMethod.PUT.name(),
        HttpMethod.PATCH.name(),
        HttpMethod.DELETE.name()
    );

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Only apply to mutating methods
        if (!IDEMPOTENT_METHODS.contains(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Skip actuator endpoints
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String idempotencyKey = httpRequest.getHeader(IDEMPOTENCY_KEY_HEADER);

        // If no idempotency key provided, proceed without idempotency checking
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.debug("No idempotency key provided for {} {}", httpRequest.getMethod(), path);
            chain.doFilter(request, response);
            return;
        }

        // Check if we have a cached response
        Optional<IdempotencyKey> cachedKey = idempotencyService.findValidKey(idempotencyKey);

        if (cachedKey.isPresent()) {
            log.info("Returning cached response for idempotency key: {}", idempotencyKey);
            returnCachedResponse(httpResponse, cachedKey.get());
            return;
        }

        // Wrap response to capture the response body
        ContentCachingResponseWrapper responseWrapper =
            new ContentCachingResponseWrapper(httpResponse);

        try {
            chain.doFilter(request, responseWrapper);

            // Only cache successful responses (2xx status codes)
            int status = responseWrapper.getStatus();
            if (status >= 200 && status < 300) {
                String responseBody = new String(
                    responseWrapper.getContentAsByteArray(),
                    StandardCharsets.UTF_8
                );
                idempotencyService.storeKey(idempotencyKey, status, responseBody);
                log.debug("Cached response for idempotency key: {}", idempotencyKey);
            }
        } finally {
            // Copy the cached content to the actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void returnCachedResponse(
            HttpServletResponse response,
            IdempotencyKey cachedKey) throws IOException {

        response.setStatus(cachedKey.getResponseStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-Idempotency-Replayed", "true");

        if (cachedKey.getResponseBody() != null && !cachedKey.getResponseBody().isEmpty()) {
            response.getWriter().write(cachedKey.getResponseBody());
        }
    }
}
