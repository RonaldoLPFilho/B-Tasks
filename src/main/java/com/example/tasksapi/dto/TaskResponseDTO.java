package com.example.tasksapi.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        boolean completed,
        LocalDate finishedAt,
        String jiraId,
        UUID tabId,
        UUID sectionId,
        Integer sortOrder,
        boolean active,
        boolean archived,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CategoryResponseDTO category,
        List<SubtaskResponseDTO> subtasks,
        List<CommentResponseDTO> comments
) {
}
