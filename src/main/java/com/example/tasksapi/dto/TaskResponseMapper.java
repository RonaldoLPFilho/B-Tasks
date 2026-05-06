package com.example.tasksapi.dto;

import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.task.element.CommentElement;
import com.example.tasksapi.domain.task.element.DueDateElement;
import com.example.tasksapi.domain.task.element.SubtaskElement;
import com.example.tasksapi.domain.task.element.TaskElement;
import com.example.tasksapi.dto.element.CommentElementResponseDTO;
import com.example.tasksapi.dto.element.DueDateElementResponseDTO;
import com.example.tasksapi.dto.element.SubtaskElementResponseDTO;
import com.example.tasksapi.dto.element.TaskElementResponseDTO;
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
                task.getElements() == null
                        ? Collections.emptyList()
                        : task.getElements().stream().map(this::toElementResponse).toList()
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

    public TaskElementResponseDTO toElementResponse(TaskElement element) {
        if (element instanceof SubtaskElement s) {
            return new SubtaskElementResponseDTO(
                    s.getId(), s.getElementType(), s.getSortOrder(), s.getCreatedAt(),
                    s.getTitle(), s.isCompleted()
            );
        } else if (element instanceof CommentElement c) {
            return new CommentElementResponseDTO(
                    c.getId(), c.getElementType(), c.getSortOrder(), c.getCreatedAt(),
                    c.getDescription(), c.getAuthor()
            );
        } else if (element instanceof DueDateElement d) {
            return new DueDateElementResponseDTO(
                    d.getId(), d.getElementType(), d.getSortOrder(), d.getCreatedAt(),
                    d.getDueDate()
            );
        }
        throw new IllegalStateException("Unknown element type: " + element.getClass().getSimpleName());
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
