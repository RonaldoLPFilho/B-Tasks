package com.example.tasksapi.dto;

public record CreatePomodoroPreferenceDTO (int sessionDuration, int breakDuration, String alarmSound) {}
