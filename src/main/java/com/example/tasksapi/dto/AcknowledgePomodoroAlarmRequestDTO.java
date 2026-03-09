package com.example.tasksapi.dto;

import com.example.tasksapi.domain.pomodoro.PomodoroMode;

public record AcknowledgePomodoroAlarmRequestDTO(PomodoroMode nextMode, boolean autoStart) {}
