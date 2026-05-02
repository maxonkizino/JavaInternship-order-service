package com.javainternshiporderservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleOrderNotFound_shouldReturnNotFound() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found with id: 123");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleOrderNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).message()).contains("Order not found with id: 123");
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
    }

    @Test
    void handleOrderItemNotFound_shouldReturnNotFound() {
        OrderItemNotFoundException ex = new OrderItemNotFoundException("Order item not found with id: 456");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleOrderItemNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).message()).contains("Order item not found with id: 456");
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    void handleItemNotFound_shouldReturnNotFound() {
        ItemNotFoundException ex = new ItemNotFoundException("Item not found with id: 789");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleItemNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).message()).contains("Item not found with id: 789");
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    void handleValidationErrors_shouldReturnBadRequest() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "field1", "Field1 is required"));
        bindingResult.addError(new FieldError("testObject", "field2", "Field2 must be positive"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("Validation failed");
        assertThat(response.getBody().message()).contains("field1");
        assertThat(response.getBody().message()).contains("Field1 is required");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequest() {
        ConstraintViolationException ex = new ConstraintViolationException("Constraint violation: email must be valid", null);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("email must be valid");
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Access denied for user");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(403);
        assertThat(response.getBody().message()).isEqualTo("Access denied");
    }

    @Test
    void handleMissingPathVariable_shouldReturnBadRequest() {
        MissingPathVariableException ex = mock(MissingPathVariableException.class);
        when(ex.getMessage()).thenReturn("Required URI template variable 'id' is not present");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleMissingPathVariable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("id");
    }

    @Test
    void handleHttpMessageNotReadable_shouldReturnBadRequest() {
        HttpInputMessage inputMessage = new HttpInputMessage() {
            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Invalid JSON format", new RuntimeException("parse"), inputMessage);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("Request body is missing or has invalid format");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error occurred");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void errorResponse_shouldContainAllFields() {
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                404,
                "Not Found",
                "Resource not found",
                java.time.LocalDateTime.now()
        );

        assertThat(errorResponse.status()).isEqualTo(404);
        assertThat(errorResponse.error()).isEqualTo("Not Found");
        assertThat(errorResponse.message()).isEqualTo("Resource not found");
        assertThat(errorResponse.timestamp()).isNotNull();
    }
}
