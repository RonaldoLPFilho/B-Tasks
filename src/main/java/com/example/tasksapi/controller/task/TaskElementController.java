package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.dto.element.CommentElementResponseDTO;
import com.example.tasksapi.dto.element.CreateCommentElementRequestDTO;
import com.example.tasksapi.dto.element.CreateDueDateElementRequestDTO;
import com.example.tasksapi.dto.element.CreateSubtaskElementRequestDTO;
import com.example.tasksapi.dto.element.DueDateElementResponseDTO;
import com.example.tasksapi.dto.element.SubtaskElementResponseDTO;
import com.example.tasksapi.dto.element.UpdateDueDateElementRequestDTO;
import com.example.tasksapi.service.task.TaskElementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskElementController {

    private final TaskElementService taskElementService;
    private final TaskResponseMapper taskResponseMapper;

    public TaskElementController(TaskElementService taskElementService, TaskResponseMapper taskResponseMapper) {
        this.taskElementService = taskElementService;
        this.taskResponseMapper = taskResponseMapper;
    }

    @PostMapping("/tasks/{taskId}/elements/subtask")
    public ResponseEntity<ApiResponseDTO<SubtaskElementResponseDTO>> createSubtask(
            @PathVariable UUID taskId,
            @RequestBody CreateSubtaskElementRequestDTO dto) {
        var effectiveDto = new CreateSubtaskElementRequestDTO(dto.title(), taskId);
        SubtaskElementResponseDTO data = (SubtaskElementResponseDTO) taskResponseMapper.toElementResponse(
                taskElementService.createSubtask(effectiveDto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Subtask created", data));
    }

    @PostMapping("/tasks/{taskId}/elements/comment")
    public ResponseEntity<ApiResponseDTO<CommentElementResponseDTO>> createComment(
            @PathVariable UUID taskId,
            @RequestBody CreateCommentElementRequestDTO dto) {
        var effectiveDto = new CreateCommentElementRequestDTO(dto.description(), taskId);
        CommentElementResponseDTO data = (CommentElementResponseDTO) taskResponseMapper.toElementResponse(
                taskElementService.createComment(effectiveDto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Comment created", data));
    }

    @PostMapping("/tasks/{taskId}/elements/due-date")
    public ResponseEntity<ApiResponseDTO<DueDateElementResponseDTO>> createDueDate(
            @PathVariable UUID taskId,
            @RequestBody CreateDueDateElementRequestDTO dto) {
        var effectiveDto = new CreateDueDateElementRequestDTO(dto.dueDate(), dto.dueTime(), taskId);
        DueDateElementResponseDTO data = (DueDateElementResponseDTO) taskResponseMapper.toElementResponse(
                taskElementService.createDueDate(effectiveDto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Due date created", data));
    }

    @PatchMapping("/elements/{elementId}/due-date")
    public ResponseEntity<ApiResponseDTO<Void>> updateDueDate(
            @PathVariable UUID elementId,
            @RequestBody UpdateDueDateElementRequestDTO dto) {
        taskElementService.updateDueDate(elementId, dto.dueDate(), dto.dueTime());
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Due date updated", null));
    }

    @DeleteMapping("/elements/{elementId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteElement(@PathVariable UUID elementId) {
        taskElementService.deleteElement(elementId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Element deleted", null));
    }

    @PatchMapping("/elements/{elementId}/toggle/{completed}")
    public ResponseEntity<ApiResponseDTO<Void>> toggleSubtask(
            @PathVariable UUID elementId,
            @PathVariable boolean completed) {
        taskElementService.toggleSubtaskCompletion(elementId, completed);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Subtask toggled", null));
    }
}
