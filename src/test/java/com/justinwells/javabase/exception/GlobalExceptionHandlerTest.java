package com.justinwells.javabase.exception;

import com.justinwells.javabase.dto.v1.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
    }

    @Test
    @DisplayName("should handle TaskNotFoundException with 404")
    void shouldHandleTaskNotFound() {
        UUID taskId = UUID.randomUUID();
        TaskNotFoundException ex = new TaskNotFoundException(taskId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTaskNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains(taskId.toString());
    }

    @Test
    @DisplayName("should handle DuplicateEventException with 409")
    void shouldHandleDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        DuplicateEventException ex = new DuplicateEventException(eventId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEvent(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
    }

    @Test
    @DisplayName("should handle HttpMessageNotReadableException with 400")
    void shouldHandleMessageNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid JSON");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed request body");
    }

    @Test
    @DisplayName("should handle MethodArgumentTypeMismatchException with 400")
    void shouldHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "invalid-uuid", UUID.class, "id", null, null
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("invalid-uuid");
    }

    @Test
    @DisplayName("should handle HttpRequestMethodNotSupportedException with 405")
    void shouldHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(405);
        assertThat(response.getBody().getMessage()).contains("PATCH");
    }

    @Test
    @DisplayName("should handle HttpMediaTypeNotSupportedException with 415")
    void shouldHandleMediaTypeNotSupported() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("text/plain");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMediaTypeNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(415);
    }

    @Test
    @DisplayName("should handle generic Exception with 500")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    @DisplayName("should include request path in error response")
    void shouldIncludeRequestPath() {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks/123");
        Exception ex = new RuntimeException("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/tasks/123");
    }
}
