package com.example.tasksapi.controller.pomodoro;

import com.example.tasksapi.domain.pomodoro.PomodoroPreferences;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreatePomodoroPreferenceDTO;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pomodoro")
public class PomodoroPreferencesController {

    private final PomodoroService pomodoroService;

    public PomodoroPreferencesController(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
    }

    @PutMapping
    public ResponseEntity<ApiResponseDTO<PomodoroPreferences>>  update(@RequestBody CreatePomodoroPreferenceDTO preferences) {
        PomodoroPreferences data = pomodoroService.updatePomodoroPreferences(preferences);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Successfully updated preferences", data));
    }
}
