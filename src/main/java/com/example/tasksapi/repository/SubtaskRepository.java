package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {
    Optional<Subtask> findByIdAndTaskUserId(UUID id, UUID userId);
}
