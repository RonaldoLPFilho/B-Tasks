package com.example.tasksapi.service.file;

public record StoredObject(
        String storedFileName,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        String storageProvider,
        String storageKey
) {
}
