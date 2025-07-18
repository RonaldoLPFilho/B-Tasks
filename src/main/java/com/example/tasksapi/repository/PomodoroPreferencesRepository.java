package com.example.tasksapi.repository;

import com.example.tasksapi.domain.pomodoro.PomodoroPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PomodoroPreferencesRepository extends JpaRepository<PomodoroPreferences, UUID> {
    PomodoroPreferences findByUserId(UUID userId);
}
