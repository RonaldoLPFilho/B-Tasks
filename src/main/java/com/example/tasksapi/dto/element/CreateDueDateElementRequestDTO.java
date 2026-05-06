package com.example.tasksapi.dto.element;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDueDateElementRequestDTO(LocalDate dueDate, UUID taskId) {}
