package com.example.tasksapi.dto;

import java.util.List;

public record ArchivedItemsPageDTO(
        List<ArchivedSearchResultDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
