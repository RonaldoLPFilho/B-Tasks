package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VaultItemResponseDTO(
        UUID id,
        String name,
        String description,
        List<VaultEntryDTO> entries,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
