package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.SectionTaskReorderRequest;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.dto.TaskResponseDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.dto.TaskSearchResultDTO;
import com.example.tasksapi.service.task.ArchiveScope;
import com.example.tasksapi.service.task.TaskSearchService;
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
    private final TaskResponseMapper taskResponseMapper;
    private final TaskSearchService taskSearchService;

    public TaskController(TaskService taskService, TaskResponseMapper taskResponseMapper,
                          TaskSearchService taskSearchService) {
        this.taskService = taskService;
        this.taskResponseMapper = taskResponseMapper;
        this.taskSearchService = taskSearchService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getAllTasks(@RequestParam(required = false) UUID tabId) {
        List<TaskResponseDTO> data = tabId != null
                ? taskResponseMapper.toTaskResponses(taskService.findByTabIdForCurrentUser(tabId))
                : taskResponseMapper.toTaskResponses(taskService.findAllForCurrentUser());
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tasks", data));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<TaskSearchResultDTO>>> searchTasks(
            @RequestParam String q,
            @RequestParam(required = false) UUID tabId,
            @RequestParam(defaultValue = "active") String scope,
            @RequestParam(required = false) Integer limit) {
        List<TaskSearchResultDTO> data = taskSearchService.search(q, tabId, limit, ArchiveScope.fromRequest(scope));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Search results", data));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> getTaskById(@PathVariable UUID taskId) {
        TaskResponseDTO data = taskResponseMapper.toTaskResponse(taskService.findByIdForCurrentUser(taskId));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task found", data));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTaskById(@PathVariable UUID taskId) {
        taskService.deleteById(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Success deleted task id " + taskId, null));
    }

    @PostMapping()
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> createTask(@RequestBody TaskDTO dto) {
        TaskResponseDTO data = taskResponseMapper.toTaskResponse(taskService.save(dto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Task created", data));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> updateTask(@PathVariable UUID taskId, @RequestBody TaskDTO dto) {
        TaskResponseDTO data = taskResponseMapper.toTaskResponse(taskService.update(taskId, dto));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task updated", data));
    }

    @PutMapping("/{taskId}/{completed}")
    public ResponseEntity<ApiResponseDTO<UUID>> completeTask(@PathVariable UUID taskId, @PathVariable boolean completed) {
        taskService.updateCompleted(taskId, completed);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task completed", taskId));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody SectionTaskReorderRequest body) {
        taskService.reorder(body);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/disable/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> disableTask(@PathVariable UUID taskId) {
        taskService.archiveTask(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task archived",  null));
    }

    @PatchMapping("/active/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> activateTask(@PathVariable UUID taskId) {
        taskService.unarchiveTask(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task unarchived",  null));
    }

    @PatchMapping("/archive/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> archiveTask(@PathVariable UUID taskId) {
        taskService.archiveTask(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task archived",  null));
    }

    @PatchMapping("/unarchive/{taskId}")
    public ResponseEntity<ApiResponseDTO<Void>> unarchiveTask(@PathVariable UUID taskId) {
        taskService.unarchiveTask(taskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task unarchived",  null));
    }
}
