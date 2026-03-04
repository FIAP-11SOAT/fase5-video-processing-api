package com.example.demo.shared.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleApiException() {
        APIException ex = new APIException(
                ErrorType.MAX_SIZE_EXCEEDED,
                400,
                new Exception()
        );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("file max size exceeded", body.getMessage());
        assertEquals(ErrorType.MAX_SIZE_EXCEEDED.getCode(), body.getErrorCode());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        Object target = new Object();
        BindingResult bindingResult =
                new BeanPropertyBindingResult(target, "request");

        bindingResult.addError(
                new FieldError(
                        "request",
                        "file",
                        "must not be null"
                )
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("file: must not be null", body.getMessage());
        assertEquals(400, body.getErrorCode());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void shouldHandleNoHandlerFoundException() {
        NoHandlerFoundException ex =
                new NoHandlerFoundException("GET", "/invalid", null);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleNoHandlerFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Not Found", body.getError());
        assertEquals("Route not found", body.getMessage());
        assertEquals(404, body.getErrorCode());
    }

    @Test
    void shouldHandleAuthenticationException_asUnauthorized() {
        AuthenticationException ex =
                new AuthenticationException("Unauthorized") {};

        ResponseEntity<ApiErrorResponse> response =
                handler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.getStatus());
        assertEquals(ErrorType.UNAUTHORIZED.getMessage(), body.getError());
        assertEquals(ErrorType.UNAUTHORIZED.getCode(), body.getErrorCode());
    }

    @Test
    void shouldHandleAccessDeniedException_asForbidden() {
        AccessDeniedException ex =
                new AccessDeniedException("Forbidden");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertEquals(ErrorType.FORBIDDEN.getMessage(), body.getError());
        assertEquals(ErrorType.FORBIDDEN.getCode(), body.getErrorCode());
    }

    @Test
    void shouldHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException ex =
                new MaxUploadSizeExceededException(10_000);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleMaxSize(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals(ErrorType.MAX_SIZE_EXCEEDED.getCode(), body.getErrorCode());
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Boom");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("Unexpected internal error", body.getMessage());
        assertEquals(999, body.getErrorCode());
    }
}
