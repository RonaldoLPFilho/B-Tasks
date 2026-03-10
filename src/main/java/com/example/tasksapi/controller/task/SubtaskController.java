package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateSubstaskRequestDTO;
import com.example.tasksapi.dto.SubtaskResponseDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.service.task.SubtaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subtasks")
public class SubtaskController {

    private final SubtaskService subtaskService;
    private final TaskResponseMapper taskResponseMapper;

    public SubtaskController(SubtaskService subtaskService, TaskResponseMapper taskResponseMapper) {
        this.subtaskService = subtaskService;
        this.taskResponseMapper = taskResponseMapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<SubtaskResponseDTO>> create(@RequestBody CreateSubstaskRequestDTO subDTO) {
        SubtaskResponseDTO data = taskResponseMapper.toSubtaskResponse(subtaskService.createSubtask(subDTO));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Subtask created", data));
    }

    @PutMapping("/{subtaskId}/{isComplete}")
    public ResponseEntity<ApiResponseDTO<Void>> changeSubtaskStatus(@PathVariable UUID subtaskId, @PathVariable boolean isComplete) {
        subtaskService.completeSubtask(subtaskId, isComplete);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Subtask status changed", null));
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable UUID subtaskId) {
        subtaskService.deleteById(subtaskId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Subtask deleted", null));
    }

}
