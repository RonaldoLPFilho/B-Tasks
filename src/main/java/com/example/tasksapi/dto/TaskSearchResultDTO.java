package com.example.tasksapi.dto;

import java.util.List;

public record TaskSearchResultDTO(
        TaskResponseDTO task,
        String tabName,
        boolean tabArchived,
        String sectionName,
        boolean sectionArchived,
        int score,
        List<TaskSearchMatchDTO> matches
) {
}
