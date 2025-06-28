package com.example.tasksapi.auth;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.ApiResponseStatus;
import com.example.tasksapi.exception.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        Exception cause = (Exception) request.getAttribute("jwt_exception");
        String message = "Unauthorized - Generic reason";

        if(cause instanceof io.jsonwebtoken.ExpiredJwtException)
            message = "Expired JWT token";

        if(cause instanceof  io.jsonwebtoken.security.SignatureException){
            message = "Invalid token signature";
        }

        ApiResponseDTO<?> apiResponseDTO = new ApiResponseDTO<>(
                ApiResponseStatus.ERROR,
                message,
                HttpStatus.UNAUTHORIZED,
                ErrorDetail.INTERNAL_ERROR
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponseDTO));
    }
}
