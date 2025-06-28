package com.example.tasksapi.controller;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.service.OpenAIService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/daily", produces = MediaType.APPLICATION_JSON_VALUE)
public class DailySummaryController {

    private final OpenAIService openAIService;

    public DailySummaryController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDTO<String>> getDailySummary(@RequestHeader("Authorization") String authHeader, @RequestParam String language) {
        String token = authHeader.substring(7);
        String summary = openAIService.generateDailySummary(token, language);

        return ResponseEntity.ok(
            ApiResponseDTO.success(HttpStatus.OK, "Daily summary generated successfully", summary)
        );
    }
} 