package com.example.tasksapi.dto;

import java.util.UUID;

public record CreateSubstaskRequestDTO(String title, UUID taskId) {}
