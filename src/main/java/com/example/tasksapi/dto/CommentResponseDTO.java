package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        String description,
        LocalDateTime createdAt,
        String author
) {
}
