package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {

    List<Section> findByTabIdOrderBySortOrderAsc(UUID tabId);

    long countByTabId(UUID tabId);

    boolean existsByIdAndTabId(UUID id, UUID tabId);

    Optional<Section> findByTabIdAndNameIgnoreCase(UUID tabId, String name);

    @Modifying
    @Query("UPDATE Section s SET s.sortOrder = :newOrder WHERE s.id = :sectionId")
    int updateSortOrder(@Param("sectionId") UUID sectionId, @Param("newOrder") int newOrder);
}
