package com.example.tasksapi.controller;

import com.example.tasksapi.auth.AuthService;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Void>> register(@RequestBody UserDTO dto) {
        boolean created =  authService.register(dto);

        if(!created) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error(HttpStatus.CONFLICT, "User already exists"));
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "User created", null)
                );
    }
}
