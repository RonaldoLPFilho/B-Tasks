package com.example.tasksapi.repository;

import com.example.tasksapi.domain.PasswordResetToken;
import com.example.tasksapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
