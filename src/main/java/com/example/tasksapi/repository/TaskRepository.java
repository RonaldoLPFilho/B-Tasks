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

    List<Task> findByTabIsNull();

    List<Task> findByTabIsNullAndSectionIsNull();

    List<Task> findByTab_IdOrderBySortOrderAsc(UUID tabId);

    List<Task> findByTab_IdAndSectionIsNullOrderBySortOrderAsc(UUID tabId);

    List<Task> findBySection_Tab_IdOrderBySortOrderAsc(UUID tabId);

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
