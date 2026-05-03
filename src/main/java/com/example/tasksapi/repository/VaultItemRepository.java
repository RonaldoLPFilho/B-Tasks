package com.example.tasksapi.repository;

import com.example.tasksapi.domain.vault.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VaultItemRepository extends JpaRepository<VaultItem, UUID> {
    List<VaultItem> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
