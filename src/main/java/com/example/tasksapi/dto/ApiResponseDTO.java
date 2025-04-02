package com.example.tasksapi.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ApiResponseDTO<T> {
    private ApiResponseStatus status;
    private String message;
    private HttpStatus httpStatus;
    private LocalDateTime timestamp;
    private T data;

    private ApiResponseDTO(ApiResponseStatus status, String message, HttpStatus httpStatus, T data) {
        this.status = status;
        this.message = message;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    public static <T> ApiResponseDTO<T> success(HttpStatus httpStatus,  String message,  T data) {
        return new ApiResponseDTO<>(ApiResponseStatus.SUCCESS, message, httpStatus, data);
    }

    public static <T> ApiResponseDTO<T> error(HttpStatus httpStatus,  String message) {
        return new ApiResponseDTO<>(ApiResponseStatus.ERROR, message, httpStatus, null);
    }

    public ApiResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public T getData() {
        return data;
    }

}
