package com.example.tasksapi.service;


import com.example.tasksapi.domain.Subtask;
import com.example.tasksapi.domain.Task;
import com.example.tasksapi.dto.CreateSubstaskRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.repository.SubtaskRepository;
import org.springframework.stereotype.Service;

@Service
public class SubtaskService {
    private final SubtaskRepository subtaskRepository;
    private final TaskService taskService;

    public SubtaskService(SubtaskRepository subtaskRepository, TaskService taskService) {
        this.subtaskRepository = subtaskRepository;
        this.taskService = taskService;
    }

    public Subtask createSubtask(CreateSubstaskRequestDTO dto) {

        Task task = taskService.findById(dto.taskId());
        Subtask sub = new Subtask(
                dto.title(),
                dto.completed(),
                task
        );

           if(!isValid(sub))
               throw new InvalidDataException("Invalid subtask");

           return subtaskRepository.save(sub);
    }

    private boolean isValid(Subtask subtask){
        return subtask.getTitle() != null &&
                !subtask.getTitle().isBlank();
    }
}
