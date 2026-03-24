package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.Tab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TabRepository extends JpaRepository<Tab, UUID> {

    List<Tab> findByUserIdAndArchivedFalseOrderBySortOrderAsc(UUID userId);

    List<Tab> findByUserIdAndArchivedTrueOrderBySortOrderAsc(UUID userId);

    List<Tab> findByUserIdOrderBySortOrderAsc(UUID userId);

    long countByUserIdAndArchivedFalse(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
