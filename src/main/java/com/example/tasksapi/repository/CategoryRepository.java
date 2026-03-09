package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserIdOrderByDefaultCategoryDescNameAsc(UUID userId);
    Optional<Category> findByUserIdAndId(UUID userId, UUID categoryId);
    Optional<Category> findByUserIdAndNameIgnoreCase(UUID userId, String name);
    Optional<Category> findByUserIdAndDefaultCategoryTrue(UUID userId);
    boolean existsByUserIdAndDefaultCategoryTrue(UUID userId);
    boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);
}
