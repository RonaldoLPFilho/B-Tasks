package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HowToDoSummaryDTO(
        UUID id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
