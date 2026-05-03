package com.example.tasksapi.repository;

import com.example.tasksapi.domain.file.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID>, JpaSpecificationExecutor<StoredFile> {
    Optional<StoredFile> findByIdAndUserId(UUID id, UUID userId);
}
