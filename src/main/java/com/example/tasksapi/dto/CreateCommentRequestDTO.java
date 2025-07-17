package com.example.tasksapi.dto;

import java.util.UUID;

public record CreateCommentRequestDTO (String description, UUID taskId) {}
