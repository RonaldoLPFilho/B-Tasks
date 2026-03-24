package com.example.tasksapi.dto;

import java.util.UUID;

public record RestoreTaskRequestDTO(
        Boolean restoreParents,
        UUID targetTabId,
        UUID targetSectionId
) {
}
