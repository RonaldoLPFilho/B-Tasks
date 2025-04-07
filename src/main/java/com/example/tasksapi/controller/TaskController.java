package com.example.tasksapi.controller;

import com.example.tasksapi.domain.Task;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Task>>> getAllTasks() {
        List<Task> data = taskService.findAll();
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tasks", data));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<Task>> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.findById(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task found", task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTaskById(@PathVariable Long taskId) {
        taskService.deleteById(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Success deleted task id " + taskId, null));
    }

    @PostMapping()
    public ResponseEntity<ApiResponseDTO<Task>> createTask(@RequestBody TaskDTO dto, @RequestHeader("Authorization") String authorizationToken) {

        String token = authorizationToken.substring(7);
        Task data = taskService.save(dto, token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Task created", data));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<Task>> updateTask(@PathVariable Long taskId, @RequestBody TaskDTO dto) {
        Task data = taskService.update(taskId, dto);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task updated", data));
    }

    @PutMapping("/{taskId}/{completed}")
    public ResponseEntity<ApiResponseDTO<Long>> completeTask(@PathVariable Long taskId, @PathVariable boolean completed) {
        taskService.updateCompleted(taskId, completed);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task completed", taskId));
    }
}
