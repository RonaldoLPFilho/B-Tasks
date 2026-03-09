package com.example.tasksapi.dto;

public record LofiTrackDTO(
        String id,
        String title,
        String file,
        String url,
        int order
) {}
