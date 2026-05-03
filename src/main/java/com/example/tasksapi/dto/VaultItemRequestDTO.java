package com.example.tasksapi.dto;

import java.util.List;

public record VaultItemRequestDTO(String name, String description, List<VaultEntryDTO> entries) {
}
