package com.example.tasksapi.repository;

import com.example.tasksapi.domain.vault.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VaultEntryRepository extends JpaRepository<VaultEntry, UUID> {
}
