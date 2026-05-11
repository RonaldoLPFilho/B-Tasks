package com.example.tasksapi.dto.element;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateDueDateElementRequestDTO(LocalDate dueDate, LocalTime dueTime) {}
