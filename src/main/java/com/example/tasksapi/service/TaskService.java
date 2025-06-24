package com.example.tasksapi.service;

import com.example.tasksapi.domain.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public List<Task> findAllByToken(String token){
        Optional<User> user = userService.extractEmailFromTokenAndReturnUser(token);

        if(user.isEmpty()){
            throw new IllegalArgumentException("User not found");
        }

        return taskRepository.findByUserId(user.get().getId());
    }

    public Task findById(long id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id " + id));
    }

    public Task save(TaskDTO dto, String token){
             User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task(dto.getTitle(), dto.getDescription(), user,  dto.getJiraId());


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

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        task.setJiraId(dto.getJiraId());
        return taskRepository.save(task);
    }

    public void updateCompleted(long taskId, boolean completed){
        Task task = findById(taskId);
        task.setCompleted(completed);
        task.setFinishDate(LocalDate.now());
        taskRepository.save(task);
    }

    private boolean isValidTask(Task task){
        return task.getTitle() != null &&
                !task.getTitle().isBlank();

    }
}
