package com.example.tasksapi.dto;

import java.util.List;

public record TaskSearchMatchDTO(
        String field,
        String label,
        String snippet,
        List<String> matchedTerms
) {
}
