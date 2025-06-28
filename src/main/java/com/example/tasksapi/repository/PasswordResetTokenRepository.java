package com.example.tasksapi.repository;

import com.example.tasksapi.domain.PasswordResetToken;
import com.example.tasksapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
