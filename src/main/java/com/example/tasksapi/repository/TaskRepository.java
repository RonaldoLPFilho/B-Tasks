package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserId(UUID userId);

    List<Task> findByUserIdOrderBySortOrderAsc(UUID userId);

    @Query("""
            SELECT t
            FROM Task t
            JOIN t.section s
            JOIN s.tab tab
            WHERE t.user.id = :userId
              AND t.active = true
              AND COALESCE(s.archived, false) = false
              AND tab.archived = false
            ORDER BY t.sortOrder ASC
            """)
    List<Task> findActiveByUserIdOrderBySortOrderAsc(@Param("userId") UUID userId);

    List<Task> findByUserIdAndCategoryIsNull(UUID userId);

    List<Task> findByTabIsNull();

    List<Task> findByTabIsNullAndSectionIsNull();

    List<Task> findByTab_IdOrderBySortOrderAsc(UUID tabId);

    List<Task> findByTab_IdAndSectionIsNullOrderBySortOrderAsc(UUID tabId);

    List<Task> findBySection_Tab_IdOrderBySortOrderAsc(UUID tabId);

    @Query("""
            SELECT t
            FROM Task t
            JOIN t.section s
            JOIN s.tab tab
            WHERE s.tab.id = :tabId
              AND t.active = true
              AND COALESCE(s.archived, false) = false
              AND tab.archived = false
            ORDER BY t.sortOrder ASC
            """)
    List<Task> findActiveBySectionTabIdOrderBySortOrderAsc(@Param("tabId") UUID tabId);

    @Query("""
            SELECT t
            FROM Task t
            JOIN t.tab tab
            WHERE tab.id = :tabId
              AND t.active = true
              AND tab.archived = false
              AND t.section IS NULL
            ORDER BY t.sortOrder ASC
            """)
    List<Task> findActiveByTabIdAndSectionIsNullOrderBySortOrderAsc(@Param("tabId") UUID tabId);

    List<Task> findBySection_IdOrderBySortOrderAsc(UUID sectionId);

    @Query("SELECT COALESCE(MAX(t.sortOrder), -1) FROM Task t WHERE t.section.id = :sectionId")
    int findMaxSortOrderBySectionId(@Param("sectionId") UUID sectionId);

    @Query("""
            SELECT DISTINCT t
            FROM Task t
            JOIN FETCH t.section s
            JOIN FETCH s.tab tab
            WHERE t.user.id = :userId
              AND (
                t.active = false
                OR COALESCE(s.archived, false) = true
                OR tab.archived = true
              )
            ORDER BY t.createdAt DESC
            """)
    List<Task> findArchivedForUser(@Param("userId") UUID userId);

    List<Task> findByCategoryId(UUID categoryId);

    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = t.sortOrder + 1 WHERE t.user.id = :userId")
    int bumpAllOrders(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = t.sortOrder + 1 WHERE t.tab.id = :tabId")
    int bumpAllOrdersByTabId(@Param("tabId") UUID tabId);

    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = t.sortOrder + 1 WHERE t.section.id = :sectionId")
    int bumpAllOrdersBySectionId(@Param("sectionId") UUID sectionId);

    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = :newOrder WHERE t.id = :taskId")
    int updateOrder(@Param("taskId") UUID taskId, @Param("newOrder") int newOrder);

    long countByUserId(UUID userId);

    @Query("SELECT t.id FROM Task t WHERE t.user.id = :userId")
    List<UUID> listIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT t.id FROM Task t WHERE t.tab.id = :tabId")
    List<UUID> listIdsByTabId(@Param("tabId") UUID tabId);

    @Query("SELECT t.id FROM Task t WHERE t.section.id = :sectionId")
    List<UUID> listIdsBySectionId(@Param("sectionId") UUID sectionId);
}
