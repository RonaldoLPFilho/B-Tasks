package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.task.element.CommentElement;
import com.example.tasksapi.domain.task.element.DueDateElement;
import com.example.tasksapi.domain.task.element.SubtaskElement;
import com.example.tasksapi.domain.task.element.TaskElement;
import com.example.tasksapi.dto.element.CreateCommentElementRequestDTO;
import com.example.tasksapi.dto.element.CreateDueDateElementRequestDTO;
import com.example.tasksapi.dto.element.CreateSubtaskElementRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.TaskElementRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class TaskElementService {

    private final TaskElementRepository taskElementRepository;
    private final TaskService taskService;
    private final AuthenticatedUserService authenticatedUserService;

    public TaskElementService(TaskElementRepository taskElementRepository,
                               TaskService taskService,
                               AuthenticatedUserService authenticatedUserService) {
        this.taskElementRepository = taskElementRepository;
        this.taskService = taskService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public SubtaskElement createSubtask(CreateSubtaskElementRequestDTO dto) {
        if (dto.title() == null || dto.title().isBlank()) {
            throw new InvalidDataException("Subtask title is required");
        }
        User user = authenticatedUserService.getCurrentUser();
        Task task = taskService.findByIdAndValidateOwnership(dto.taskId(), user.getId());
        return taskElementRepository.save(new SubtaskElement(dto.title().trim(), task));
    }

    public CommentElement createComment(CreateCommentElementRequestDTO dto) {
        if (dto.description() == null || dto.description().isBlank() || dto.taskId() == null) {
            throw new InvalidDataException("Invalid comment data");
        }
        User user = authenticatedUserService.getCurrentUser();
        Task task = taskService.findByIdAndValidateOwnership(dto.taskId(), user.getId());
        return taskElementRepository.save(new CommentElement(dto.description().trim(), user.getUsername(), task));
    }

    public void deleteElement(UUID elementId) {
        User user = authenticatedUserService.getCurrentUser();
        TaskElement element = taskElementRepository.findByIdAndTaskUserId(elementId, user.getId())
                .orElseThrow(() -> new NotFoundException("Element not found with id " + elementId));
        taskElementRepository.delete(element);
    }

    public DueDateElement createDueDate(CreateDueDateElementRequestDTO dto) {
        if (dto.dueDate() == null || dto.taskId() == null) {
            throw new InvalidDataException("Due date and taskId are required");
        }
        User user = authenticatedUserService.getCurrentUser();
        Task task = taskService.findByIdAndValidateOwnership(dto.taskId(), user.getId());
        return taskElementRepository.save(new DueDateElement(dto.dueDate(), dto.dueTime(), task));
    }

    public void updateDueDate(UUID elementId, LocalDate dueDate, LocalTime dueTime) {
        if (dueDate == null) {
            throw new InvalidDataException("Due date is required");
        }
        User user = authenticatedUserService.getCurrentUser();
        TaskElement element = taskElementRepository.findByIdAndTaskUserId(elementId, user.getId())
                .orElseThrow(() -> new NotFoundException("Element not found with id " + elementId));
        if (!(element instanceof DueDateElement dd)) {
            throw new InvalidDataException("Element is not a due date");
        }
        dd.setDueDate(dueDate);
        dd.setDueTime(dueTime);
        taskElementRepository.save(dd);
    }

    public void toggleSubtaskCompletion(UUID elementId, boolean completed) {
        User user = authenticatedUserService.getCurrentUser();
        TaskElement element = taskElementRepository.findByIdAndTaskUserId(elementId, user.getId())
                .orElseThrow(() -> new NotFoundException("Element not found with id " + elementId));
        if (!(element instanceof SubtaskElement subtask)) {
            throw new InvalidDataException("Element is not a subtask");
        }
        subtask.setCompleted(completed);
        taskElementRepository.save(subtask);
    }
}
