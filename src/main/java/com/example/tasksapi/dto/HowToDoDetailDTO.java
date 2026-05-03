package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HowToDoDetailDTO(
        UUID id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
