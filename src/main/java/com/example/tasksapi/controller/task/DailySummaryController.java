package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.service.task.TaskSummaryDailyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/daily", produces = MediaType.APPLICATION_JSON_VALUE)
public class DailySummaryController {

    private final TaskSummaryDailyService taskSummaryDailyService;

    public DailySummaryController(TaskSummaryDailyService taskSummaryDailyService) {
        this.taskSummaryDailyService = taskSummaryDailyService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDTO<String>> getDailySummary(@RequestHeader("Authorization") String authHeader, @RequestParam String language) {
        String token = authHeader.substring(7);
        String summary = taskSummaryDailyService.generateDailySummary(token, language);

        return ResponseEntity.ok(
            ApiResponseDTO.success(HttpStatus.OK, "Daily summary generated successfully", summary)
        );
    }
} 