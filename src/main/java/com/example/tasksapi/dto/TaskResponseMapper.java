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
                task.isArchived(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                toCategoryResponse(task.getCategory()),
                task.getSubtasks() == null ? Collections.emptyList() : task.getSubtasks().stream().map(this::toSubtaskResponse).toList(),
                task.getComments() == null ? Collections.emptyList() : task.getComments().stream().map(this::toCommentResponse).toList()
        );
    }

    public SectionResponseDTO toSectionResponse(Section section) {
        return toSectionResponse(section, false);
    }

    public SectionResponseDTO toSectionResponse(Section section, boolean includeArchivedTasks) {
        return new SectionResponseDTO(
                section.getId(),
                section.getName(),
                section.isArchived(),
                section.getSortOrder(),
                section.getCreatedAt(),
                section.getUpdatedAt(),
                section.getTasks() == null
                        ? Collections.emptyList()
                        : section.getTasks().stream()
                        .filter(task -> includeArchivedTasks || !task.isArchived())
                        .map(this::toTaskResponse)
                        .toList()
        );
    }

    public TabResponseDTO toTabResponse(Tab tab) {
        return toTabResponse(tab, false);
    }

    public TabResponseDTO toTabResponse(Tab tab, boolean includeArchivedContent) {
        return new TabResponseDTO(
                tab.getId(),
                tab.getName(),
                tab.isArchived(),
                tab.getSortOrder(),
                tab.getCreatedAt(),
                tab.getUpdatedAt(),
                tab.getSections() == null
                        ? Collections.emptyList()
                        : tab.getSections().stream()
                        .filter(section -> includeArchivedContent || !section.isArchived())
                        .map(section -> toSectionResponse(section, includeArchivedContent))
                        .toList()
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
        return toSectionResponses(sections, false);
    }

    public List<SectionResponseDTO> toSectionResponses(List<Section> sections, boolean includeArchivedTasks) {
        return sections.stream().map(section -> toSectionResponse(section, includeArchivedTasks)).toList();
    }

    public List<TabResponseDTO> toTabResponses(List<Tab> tabs) {
        return toTabResponses(tabs, false);
    }

    public List<TabResponseDTO> toTabResponses(List<Tab> tabs, boolean includeArchivedContent) {
        return tabs.stream().map(tab -> toTabResponse(tab, includeArchivedContent)).toList();
    }

    public List<CategoryResponseDTO> toCategoryResponses(List<Category> categories) {
        return categories.stream().map(this::toCategoryResponse).toList();
    }
}
