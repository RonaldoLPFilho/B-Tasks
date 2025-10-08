package com.example.tasksapi.dto;

import java.util.List;
import java.util.UUID;

public record ReorderTasksRequest(List<UUID> orderedIds) {}