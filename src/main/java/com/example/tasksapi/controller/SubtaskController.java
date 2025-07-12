package com.example.tasksapi.controller;

import com.example.tasksapi.domain.Subtask;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateSubstaskRequestDTO;
import com.example.tasksapi.service.SubtaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subtask")
public class SubtaskController {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Subtask>> create(@RequestBody CreateSubstaskRequestDTO subDTO) {
        Subtask data = subtaskService.createSubtask(subDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Subtask created", data));
    }
}
