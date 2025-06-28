package com.example.tasksapi.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException{

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorDetail.INTERNAL_ERROR);
    }
}
