package com.example.tasksapi.service.task;

import com.example.tasksapi.exception.InvalidDataException;

import java.util.Locale;

public enum ArchiveScope {
    ACTIVE,
    ARCHIVED,
    ALL;

    public static ArchiveScope fromRequest(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return ACTIVE;
        }

        return switch (rawValue.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> ACTIVE;
            case "ARCHIVED" -> ARCHIVED;
            case "ALL" -> ALL;
            default -> throw new InvalidDataException("Invalid archive scope: " + rawValue);
        };
    }
}
