package com.example.tasksapi.dto;

import java.util.UUID;

public record RestoreSectionRequestDTO(
        Boolean restoreParents,
        UUID targetTabId
) {
}
