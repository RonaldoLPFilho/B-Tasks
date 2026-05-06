package com.example.tasksapi.dto.element;

import java.util.UUID;

public record CreateSubtaskElementRequestDTO(String title, UUID taskId) {}
