package com.example.tasksapi.dto.element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record DueDateElementResponseDTO(
        UUID id,
        String elementType,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDate dueDate,
        LocalTime dueTime
) implements TaskElementResponseDTO {}
