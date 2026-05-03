package com.example.tasksapi.dto;

import java.time.Instant;

public record VaultUnlockResponseDTO(String token, Instant expiresAt) {
}
