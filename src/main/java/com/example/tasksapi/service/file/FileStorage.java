package com.example.tasksapi.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorage {
    StoredObject save(UUID userId, UUID fileId, MultipartFile file);

    Resource load(String storageKey);

    void delete(String storageKey);
}
