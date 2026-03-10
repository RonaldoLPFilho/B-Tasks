package com.example.tasksapi.dto;

import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        String name,
        String color,
        boolean defaultCategory
) {
}
