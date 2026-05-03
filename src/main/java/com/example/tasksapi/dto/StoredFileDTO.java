package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoredFileDTO(
        UUID id,
        String originalFileName,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        LocalDateTime uploadedAt
) {
}
