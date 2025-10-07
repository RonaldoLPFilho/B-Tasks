package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

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
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return taskRepository.findByUserId(user.getId()).stream()
                .sorted(Comparator.comparingInt(Task::getSortOrder))
                .toList();
    }

    public Task findById(UUID id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id " + id));
    }

    @Transactional
    public Task save(TaskDTO dto, String token){
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryService.findById(dto.getCategoryId());

        Task task = new Task(dto.getTitle(), dto.getDescription(), user, dto.getJiraId(), category);

        if(!isValidTask(task)){
            throw new InvalidDataException("Invalid task");
        }

        taskRepository.bumpAllOrders(user.getId());

        task.setSortOrder(0);

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteById(UUID id){
        taskRepository.deleteById(id);
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public void reorder(String token, List<UUID> orderedIds) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<UUID> current = taskRepository.listIdsByUserId(user.getId());
        if (current.size() != orderedIds.size()) {
            throw new InvalidDataException("List size mismatch");
        }
        if (!asSet(current).equals(asSet(orderedIds))) {
            throw new InvalidDataException("IDs don't belong to user or are duplicated");
        }

        // Atribui índices sequenciais
        for (int i = 0; i < orderedIds.size(); i++) {
            taskRepository.updateOrder(orderedIds.get(i), i);
        }
    }

    private Set<UUID> asSet(List<UUID> list) {
        return new HashSet<>(list);
    }


    private boolean isValidTask(Task task){
        return task.getTitle() != null &&
                !task.getTitle().isBlank();
    }
}
