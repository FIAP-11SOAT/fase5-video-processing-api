package com.example.demo.shared.exceptions;

import lombok.Getter;

@Getter
public enum ErrorType {

    UNAUTHORIZED(1, "unauthorized request", "unauthorized request"),
    FORBIDDEN(2, "forbidden access", "forbidden access"),
    UTILITY_CLASS_ERROR(3, "", ""),
    MAX_SIZE_EXCEEDED(4, "file is too large","file max size exceeded"),
    FILE_CANNOT_BE_EMPTY(5, "empty file", "file can not be empty"),
    FILE_FORMAT_INVALID(6, "invalid file format", "file format is invalid");

    private final int code;
    private final String name;
    public final String message;

    ErrorType(int code, String name, String message){
        this.code = code;
        this.name = name;
        this.message = message;
    }
}
