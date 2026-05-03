package com.example.tasksapi.repository;

import com.example.tasksapi.domain.howtodo.HowToDoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface HowToDoDocumentRepository extends JpaRepository<HowToDoDocument, UUID>, JpaSpecificationExecutor<HowToDoDocument> {
    Page<HowToDoDocument> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Optional<HowToDoDocument> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
}
