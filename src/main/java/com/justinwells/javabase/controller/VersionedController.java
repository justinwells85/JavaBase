package com.justinwells.javabase.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base controller providing API versioning support.
 *
 * <p>All versioned controllers should extend this class to ensure
 * consistent version headers across the API.
 */
@RestController
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod") // Intentionally abstract to prevent instantiation
public abstract class VersionedController {

    /**
     * API version 1 base path.
     */
    public static final String API_V1 = "/api/v1";

    /**
     * Current API version header value.
     */
    protected static final String CURRENT_VERSION = "v1";

    /**
     * Adds version headers to all responses.
     *
     * @param response the HTTP response
     */
    @ModelAttribute
    public void addVersionHeaders(HttpServletResponse response) {
        response.setHeader("API-Version", CURRENT_VERSION);
    }

    /**
     * Adds deprecation headers to responses from deprecated endpoints.
     * Override this method in deprecated controllers.
     *
     * @param response   the HTTP response
     * @param sunsetDate the date when this version will be removed (ISO format)
     * @param successor  the path to the successor version
     */
    protected void addDeprecationHeaders(
            HttpServletResponse response,
            String sunsetDate,
            String successor) {
        response.setHeader("Deprecation", "true");
        response.setHeader("Sunset", sunsetDate);
        response.setHeader("Link", "<" + successor + ">; rel=\"successor-version\"");
    }
}
