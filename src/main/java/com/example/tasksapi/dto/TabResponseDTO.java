package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TabResponseDTO(
        UUID id,
        String name,
        boolean archived,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SectionResponseDTO> sections
) {
}
