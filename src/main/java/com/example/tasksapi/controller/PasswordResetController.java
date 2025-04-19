package com.example.tasksapi.controller;

import com.example.tasksapi.auth.PasswordResetService;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.ResetPasswordDTO;
import com.example.tasksapi.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService resetService;
    private final EmailService emailService;

    public PasswordResetController(PasswordResetService resetService, EmailService emailService) {
        this.resetService = resetService;
        this.emailService = emailService;
    }

    @PostMapping("forgot-password")
    public ResponseEntity<ApiResponseDTO<Void>> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = resetService.createPasswordResetToken(email);

        emailService.sendPasswordResetEmail(email, token);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "E-mail de recuperacao enviado", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(@RequestBody ResetPasswordDTO dto) {
        resetService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Reset password enviado", null));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponseDTO<Void>> validateToken(@RequestParam String token) {
        resetService.validateToken(token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Token válido", null));
    }
}
