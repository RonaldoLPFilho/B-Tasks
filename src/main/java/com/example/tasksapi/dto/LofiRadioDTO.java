package com.example.tasksapi.dto;

import java.util.List;

public record LofiRadioDTO(
        String id,
        String name,
        String slug,
        List<LofiTrackDTO> tracks
) {}
