package com.example.tasksapi.dto;

import com.example.tasksapi.domain.pomodoro.PomodoroMode;
import com.example.tasksapi.domain.pomodoro.PomodoroStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PomodoroTimerStateDTO(
        UUID id,
        PomodoroMode mode,
        PomodoroStatus status,
        LocalDateTime startedAt,
        LocalDateTime endsAt,
        int remainingSeconds,
        boolean alarmAcknowledged
) {}
