package com.example.tasksapi.dto.element;

import java.util.UUID;

public record CreateCommentElementRequestDTO(String description, UUID taskId) {}
