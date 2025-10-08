package com.example.tasksapi.repository;

import com.example.tasksapi.domain.UserLanguagePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLanguagePreferenceRepository extends JpaRepository<UserLanguagePreference, Long> {
    UserLanguagePreference getByUserId(UUID userId);
}
