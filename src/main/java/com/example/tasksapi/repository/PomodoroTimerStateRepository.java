package com.example.tasksapi.repository;

import com.example.tasksapi.domain.pomodoro.PomodoroTimerState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PomodoroTimerStateRepository extends JpaRepository<PomodoroTimerState, UUID> {
    Optional<PomodoroTimerState> findByUserId(UUID userId);
}
