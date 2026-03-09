package com.example.tasksapi.dto;

import java.util.List;
import java.util.UUID;

public record SectionTaskReorderRequest(UUID tabId, List<SectionUpdate> sectionUpdates) {

    public record SectionUpdate(UUID sectionId, List<UUID> orderedIds) {}
}
