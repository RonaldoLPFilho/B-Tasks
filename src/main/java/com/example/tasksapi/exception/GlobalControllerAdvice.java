package com.example.tasksapi.exception;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.ApiResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice

public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseDTO> handleHolmesErrorOnIntegrationException(NotFoundException ex) {
        ApiResponseDTO errorResponse = new ApiResponseDTO(
                ApiResponseStatus.ERROR,
                ex.getMessage(),
                ex.getHttpStatus(),
                ex.getErrorDetail()
        );
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<ErrorDetail>> handleUnauthorizedException(UnauthorizedException ex) {

        ErrorDetail data = ex.getErrorDetail();

        ApiResponseDTO<ErrorDetail> errorResponse = new ApiResponseDTO<>(
                ApiResponseStatus.ERROR,
                ex.getMessage(),
                ex.getHttpStatus(),
                data
        );
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponseDTO> handleInvalidDataException(InvalidDataException ex) {
        ApiResponseDTO errorResponse = new ApiResponseDTO(
                ApiResponseStatus.ERROR,
                ex.getMessage(),
                ex.getHttpStatus(),
                ex.getErrorDetail()
        );
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<?>> handleGenericException(Exception ex) {
        logger.error(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(ApiResponseStatus.ERROR,
                        "Erro interno não tratado",
                        HttpStatus.BAD_REQUEST,
                        ErrorDetail.GENERIC_ERROR)
                );
    }
}
