package com.example.tasksapi.dto;

import java.util.List;

public record TaskSearchResultDTO(
        TaskResponseDTO task,
        String tabName,
        String sectionName,
        int score,
        List<TaskSearchMatchDTO> matches
) {
}
