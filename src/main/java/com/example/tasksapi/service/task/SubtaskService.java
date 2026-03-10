package com.example.tasksapi.service.task;


import com.example.tasksapi.domain.task.Subtask;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.CreateSubstaskRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.SubtaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SubtaskService {
    private final SubtaskRepository subtaskRepository;
    private final TaskService taskService;
    private final AuthenticatedUserService authenticatedUserService;

    public SubtaskService(SubtaskRepository subtaskRepository, TaskService taskService,
                          AuthenticatedUserService authenticatedUserService) {
        this.subtaskRepository = subtaskRepository;
        this.taskService = taskService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public Subtask createSubtask(CreateSubstaskRequestDTO dto) {
        User user = authenticatedUserService.getCurrentUser();
        Task task = taskService.findByIdAndValidateOwnership(dto.taskId(), user.getId());
        Subtask sub = new Subtask(
                dto.title(),
                task
        );

           if(!isValid(sub))
               throw new InvalidDataException("Invalid subtask");

           return subtaskRepository.save(sub);
    }

    public void completeSubtask(UUID subTaskId, boolean isComplete) {
        User user = authenticatedUserService.getCurrentUser();
        Subtask sub = subtaskRepository.findByIdAndTaskUserId(subTaskId, user.getId())
                .orElseThrow(() -> new NotFoundException("Subtask not found with id " + subTaskId));

        sub.setCompleted(isComplete);
        subtaskRepository.save(sub);
    }


    public void deleteById(UUID id) {
        User user = authenticatedUserService.getCurrentUser();
        Subtask subtask = subtaskRepository.findByIdAndTaskUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Subtask not found with id " + id));
        subtaskRepository.delete(subtask);
    }


    private boolean isValid(Subtask subtask){
        return subtask.getTitle() != null &&
                !subtask.getTitle().isBlank();
    }
}
