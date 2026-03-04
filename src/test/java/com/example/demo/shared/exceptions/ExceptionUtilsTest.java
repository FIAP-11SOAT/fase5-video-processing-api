package com.example.demo.shared.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionUtilsTest {

    @Test
    void shouldCreateApiExceptionWithGivenParams() {
        Exception cause = new RuntimeException("original error");

        APIException ex = ExceptionUtils.exception(
                ErrorType.FILE_NOT_FOUND,
                404,
                cause
        );

        assertNotNull(ex);
        assertEquals(ErrorType.FILE_NOT_FOUND.getMessage(), ex.getErrorCodeMessage());
        assertEquals(404, ex.getHttpStatus());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void shouldCreateBadRequestException() {
        Exception cause = new IllegalArgumentException("invalid input");

        APIException ex = ExceptionUtils.badRequest(
                ErrorType.FILE_FORMAT_INVALID,
                cause
        );

        assertNotNull(ex);
        assertEquals(ErrorType.FILE_FORMAT_INVALID.getMessage(), ex.getErrorCodeMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getHttpStatus());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void shouldCreateInternalServerErrorException() {
        Exception cause = new RuntimeException("unexpected error");

        APIException ex = ExceptionUtils.internalError(
                ErrorType.INTERNAL_ERROR,
                cause
        );

        assertNotNull(ex);
        assertEquals(ErrorType.INTERNAL_ERROR.getMessage(), ex.getErrorCodeMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getHttpStatus());
        assertEquals(cause, ex.getCause());
    }
}
