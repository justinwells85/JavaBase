package com.justinwells.javabase.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VersionedController Tests")
class VersionedControllerTest {

    private TestVersionedController controller;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        controller = new TestVersionedController();
        response = new MockHttpServletResponse();
    }

    /**
     * Concrete implementation for testing the abstract VersionedController.
     */
    static class TestVersionedController extends VersionedController {
        // Expose protected method for testing
        public void testAddDeprecationHeaders(HttpServletResponse response, String sunsetDate, String successor) {
            addDeprecationHeaders(response, sunsetDate, successor);
        }
    }

    @Nested
    @DisplayName("Constants")
    class ConstantTests {

        @Test
        @DisplayName("API_V1 should be /api/v1")
        void apiV1ShouldBeCorrect() {
            assertThat(VersionedController.API_V1).isEqualTo("/api/v1");
        }
    }

    @Nested
    @DisplayName("addVersionHeaders()")
    class AddVersionHeadersTests {

        @Test
        @DisplayName("should add API-Version header to response")
        void shouldAddApiVersionHeader() {
            controller.addVersionHeaders(response);

            assertThat(response.getHeader("API-Version")).isEqualTo("v1");
        }
    }

    @Nested
    @DisplayName("addDeprecationHeaders()")
    class AddDeprecationHeadersTests {

        @Test
        @DisplayName("should add Deprecation header")
        void shouldAddDeprecationHeader() {
            controller.testAddDeprecationHeaders(response, "2025-12-31", "/api/v2/tasks");

            assertThat(response.getHeader("Deprecation")).isEqualTo("true");
        }

        @Test
        @DisplayName("should add Sunset header with date")
        void shouldAddSunsetHeader() {
            controller.testAddDeprecationHeaders(response, "2025-12-31", "/api/v2/tasks");

            assertThat(response.getHeader("Sunset")).isEqualTo("2025-12-31");
        }

        @Test
        @DisplayName("should add Link header with successor version")
        void shouldAddLinkHeader() {
            controller.testAddDeprecationHeaders(response, "2025-12-31", "/api/v2/tasks");

            assertThat(response.getHeader("Link")).isEqualTo("</api/v2/tasks>; rel=\"successor-version\"");
        }

        @Test
        @DisplayName("should set all deprecation headers correctly")
        void shouldSetAllDeprecationHeaders() {
            String sunsetDate = "2026-06-30";
            String successor = "/api/v3/resources";

            controller.testAddDeprecationHeaders(response, sunsetDate, successor);

            assertThat(response.getHeader("Deprecation")).isEqualTo("true");
            assertThat(response.getHeader("Sunset")).isEqualTo(sunsetDate);
            assertThat(response.getHeader("Link")).isEqualTo("<" + successor + ">; rel=\"successor-version\"");
        }
    }
}
