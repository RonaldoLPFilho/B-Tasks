package com.example.tasksapi.dto;

import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Comment;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Subtask;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TaskResponseMapper {

    public TaskResponseDTO toTaskResponse(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getFinishedAt(),
                task.getJiraId(),
                task.getTabId(),
                task.getSectionId(),
                task.getSortOrder(),
                task.isActive(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                toCategoryResponse(task.getCategory()),
                task.getSubtasks() == null ? Collections.emptyList() : task.getSubtasks().stream().map(this::toSubtaskResponse).toList(),
                task.getComments() == null ? Collections.emptyList() : task.getComments().stream().map(this::toCommentResponse).toList()
        );
    }

    public SectionResponseDTO toSectionResponse(Section section) {
        return new SectionResponseDTO(
                section.getId(),
                section.getName(),
                section.getSortOrder(),
                section.getCreatedAt(),
                section.getUpdatedAt(),
                section.getTasks() == null ? Collections.emptyList() : section.getTasks().stream().map(this::toTaskResponse).toList()
        );
    }

    public TabResponseDTO toTabResponse(Tab tab) {
        return new TabResponseDTO(
                tab.getId(),
                tab.getName(),
                tab.isArchived(),
                tab.getSortOrder(),
                tab.getCreatedAt(),
                tab.getUpdatedAt(),
                tab.getSections() == null ? Collections.emptyList() : tab.getSections().stream().map(this::toSectionResponse).toList()
        );
    }

    public CategoryResponseDTO toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.isDefaultCategory()
        );
    }

    public SubtaskResponseDTO toSubtaskResponse(Subtask subtask) {
        return new SubtaskResponseDTO(
                subtask.getId(),
                subtask.getTitle(),
                subtask.isCompleted()
        );
    }

    public CommentResponseDTO toCommentResponse(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getDescription(),
                comment.getCreatedAt(),
                comment.getAuthor()
        );
    }

    public List<TaskResponseDTO> toTaskResponses(List<Task> tasks) {
        return tasks.stream().map(this::toTaskResponse).toList();
    }

    public List<SectionResponseDTO> toSectionResponses(List<Section> sections) {
        return sections.stream().map(this::toSectionResponse).toList();
    }

    public List<TabResponseDTO> toTabResponses(List<Tab> tabs) {
        return tabs.stream().map(this::toTabResponse).toList();
    }

    public List<CategoryResponseDTO> toCategoryResponses(List<Category> categories) {
        return categories.stream().map(this::toCategoryResponse).toList();
    }
}
