package com.example.tasksapi.dto.element;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentElementResponseDTO(
        UUID id,
        String elementType,
        int sortOrder,
        LocalDateTime createdAt,
        String description,
        String author
) implements TaskElementResponseDTO {}
