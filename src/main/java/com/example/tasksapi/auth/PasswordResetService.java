package com.example.tasksapi.auth;

import com.example.tasksapi.domain.PasswordResetToken;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.PasswordResetTokenRepository;
import com.example.tasksapi.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userService.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        tokenRepository.findByUserId(user.getId())
                .ifPresent(token -> {
                    tokenRepository.deleteByUserId(user.getId());
                    tokenRepository.flush();
                });

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expirationDate);
        tokenRepository.save(resetToken);
        return token;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        if(resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("Token is expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.registerUser(user);

        tokenRepository.delete(resetToken);
    }

    public void validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        if(resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("Token is expired");
        }
    }
}

