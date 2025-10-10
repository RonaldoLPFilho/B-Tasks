package com.example.tasksapi.controller.task;

import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.ReorderTasksRequest;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.service.task.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDTO<List<Task>>> getAllTasks(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        List<Task> data = taskService.findAllByToken(token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tasks", data));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<Task>> getTaskById(@PathVariable UUID taskId) {
        Task task = taskService.findById(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task found", task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTaskById(@PathVariable UUID taskId) {
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
    public ResponseEntity<ApiResponseDTO<Task>> updateTask(@PathVariable UUID taskId, @RequestBody TaskDTO dto) {
        Task data = taskService.update(taskId, dto);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task updated", data));
    }

    @PutMapping("/{taskId}/{completed}")
    public ResponseEntity<ApiResponseDTO<UUID>> completeTask(@PathVariable UUID taskId, @PathVariable boolean completed) {
        taskService.updateCompleted(taskId, completed);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task completed", taskId));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody ReorderTasksRequest body, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        taskService.reorder(token, body.orderedIds());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/disable/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> disableTask(@PathVariable UUID taskId) {
        taskService.disableTask(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task archived",  null));
    }

    @PatchMapping("/active/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> activateTask(@PathVariable UUID taskId) {
        taskService.activeTask(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task activated",  null));
    }
}
