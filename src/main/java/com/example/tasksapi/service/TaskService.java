package com.example.tasksapi.service;

import com.example.tasksapi.domain.Task;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll(){
        return taskRepository.findAll();
    }

    public Task findById(long id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id " + id));
    }

    public Task save(TaskDTO dto){
        Task task = new Task(dto.getName(), dto.getDescription());

        if(!isValidTask(task)){
            throw new IllegalArgumentException("Invalid task");
        }

        return taskRepository.save(task);
    }

    public void deleteById(long id){
        taskRepository.deleteById(id);
    }

    public Task update(Long taskId, TaskDTO dto){
        Task task = findById(taskId);

        task.setTitle(dto.getName());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        return taskRepository.save(task);

    }


    private boolean isValidTask(Task task){
        return task.getTitle() != null &&
                !task.getTitle().isBlank();

    }
}
