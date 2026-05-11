package com.example.tasksapi.dto.element;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateDueDateElementRequestDTO(LocalDate dueDate, LocalTime dueTime, UUID taskId) {}
