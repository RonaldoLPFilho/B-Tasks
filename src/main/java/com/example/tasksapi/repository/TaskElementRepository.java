package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.element.TaskElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskElementRepository extends JpaRepository<TaskElement, UUID> {
    Optional<TaskElement> findByIdAndTaskUserId(UUID id, UUID userId);
}
