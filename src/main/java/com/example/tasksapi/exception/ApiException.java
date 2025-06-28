package com.example.tasksapi.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final ErrorDetail errorDetail;

    public ApiException(String message, HttpStatus httpStatus, ErrorDetail errorDetail) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorDetail = errorDetail;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorDetail getErrorDetail() {
        return errorDetail;
    }

}
