package com.example.tasksapi.controller;

import com.example.tasksapi.auth.PasswordResetService;
import com.example.tasksapi.dto.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService resetService;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("forgot-password")
    public ResponseEntity<ApiResponseDTO<Void>> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = resetService.createPasswordResetToken(email);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "E-mail de recuperacao enviado", null));
    }
}
