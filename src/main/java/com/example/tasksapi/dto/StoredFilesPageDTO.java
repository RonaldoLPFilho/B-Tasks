package com.example.tasksapi.dto;

import java.util.List;

public record StoredFilesPageDTO(
        List<StoredFileDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
