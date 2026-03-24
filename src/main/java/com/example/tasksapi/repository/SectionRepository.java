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

    @Query("""
            SELECT s
            FROM Section s
            WHERE s.tab.id = :tabId
              AND COALESCE(s.archived, false) = false
            ORDER BY s.sortOrder ASC
            """)
    List<Section> findActiveByTabIdOrderBySortOrderAsc(@Param("tabId") UUID tabId);

    long countByTabId(UUID tabId);

    @Query("""
            SELECT COUNT(s)
            FROM Section s
            WHERE s.tab.id = :tabId
              AND COALESCE(s.archived, false) = false
            """)
    long countActiveByTabId(@Param("tabId") UUID tabId);

    boolean existsByIdAndTabId(UUID id, UUID tabId);

    Optional<Section> findByTabIdAndNameIgnoreCase(UUID tabId, String name);

    @Query("""
            SELECT DISTINCT s
            FROM Section s
            JOIN FETCH s.tab tab
            LEFT JOIN FETCH s.tasks tasks
            WHERE tab.user.id = :userId
              AND (
                COALESCE(s.archived, false) = true
                OR tab.archived = true
              )
            ORDER BY s.sortOrder ASC
            """)
    List<Section> findArchivedForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Section s SET s.sortOrder = :newOrder WHERE s.id = :sectionId")
    int updateSortOrder(@Param("sectionId") UUID sectionId, @Param("newOrder") int newOrder);
}
