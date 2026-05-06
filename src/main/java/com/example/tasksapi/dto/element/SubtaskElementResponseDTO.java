package com.example.tasksapi.dto.element;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubtaskElementResponseDTO(
        UUID id,
        String elementType,
        int sortOrder,
        LocalDateTime createdAt,
        String title,
        boolean completed
) implements TaskElementResponseDTO {}
