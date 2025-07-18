package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.Category;
import com.example.tasksapi.domain.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.CategoryService;
import com.example.tasksapi.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public TaskService(TaskRepository taskRepository, UserService userService, CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public List<Task> findAllByToken(String token){
        Optional<User> user = userService.extractEmailFromTokenAndReturnUser(token);

        if(user.isEmpty()){
            throw new NotFoundException("User not found");
        }

        return taskRepository.findByUserId(user.get().getId());
    }

    public Task findById(UUID id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id " + id));
    }

    public Task save(TaskDTO dto, String token){
         User user = userService.extractEmailFromTokenAndReturnUser(token)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryService.findById(dto.getCategoryId());

        Task task = new Task(dto.getTitle(), dto.getDescription(), user,  dto.getJiraId(), category);


        if(!isValidTask(task)){
            throw new InvalidDataException("Invalid task");
        }

        return taskRepository.save(task);
    }

    public void deleteById(UUID id){
        taskRepository.deleteById(id);
    }

    public Task update(UUID taskId, TaskDTO dto){
        Task task = findById(taskId);

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if(dto.isCompleted()){
            task.setCompleted(true);
            task.setFinishDate(LocalDate.now());
        }
        else{
            task.setCompleted(false);
            task.setFinishDate(null);
        }


        task.setJiraId(dto.getJiraId());
        return taskRepository.save(task);
    }

    public void updateCompleted(UUID taskId, boolean completed){
        Task task = findById(taskId);
        task.setCompleted(completed);

        if(completed){
            task.setFinishDate(LocalDate.now());
        }
        else {
            task.setFinishDate(null);
        }

        taskRepository.save(task);
    }

    public boolean existsById(UUID id){
        return taskRepository.existsById(id);
    }

    private boolean isValidTask(Task task){
        return task.getTitle() != null &&
                !task.getTitle().isBlank();
    }
}
