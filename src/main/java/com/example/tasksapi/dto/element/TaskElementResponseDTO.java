package com.example.tasksapi.dto.element;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "elementType", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubtaskElementResponseDTO.class, name = "SUBTASK"),
        @JsonSubTypes.Type(value = CommentElementResponseDTO.class, name = "COMMENT"),
        @JsonSubTypes.Type(value = DueDateElementResponseDTO.class, name = "DUE_DATE")
})
public sealed interface TaskElementResponseDTO permits SubtaskElementResponseDTO, CommentElementResponseDTO, DueDateElementResponseDTO {
    UUID id();
    String elementType();
    int sortOrder();
    LocalDateTime createdAt();
}
