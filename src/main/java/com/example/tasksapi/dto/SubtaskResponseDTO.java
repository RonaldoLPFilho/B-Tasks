package com.example.tasksapi.dto;

import java.util.UUID;

public record SubtaskResponseDTO(
        UUID id,
        String title,
        boolean completed
) {
}
