package com.example.tasksapi.dto;

import java.util.List;
import java.util.UUID;

public record ArchivedSearchResultDTO(
        ArchivedItemTypeDTO type,
        UUID id,
        String title,
        String subtitle,
        TabResponseDTO tab,
        SectionResponseDTO section,
        TaskResponseDTO task,
        UUID parentTabId,
        String parentTabName,
        boolean parentTabArchived,
        UUID parentSectionId,
        String parentSectionName,
        boolean parentSectionArchived,
        int score,
        List<TaskSearchMatchDTO> matches
) {
}
