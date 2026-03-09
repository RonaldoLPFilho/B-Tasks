package com.example.tasksapi.controller.pomodoro;

import com.example.tasksapi.dto.AcknowledgePomodoroAlarmRequestDTO;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.PomodoroTimerStateDTO;
import com.example.tasksapi.dto.ResetPomodoroTimerRequestDTO;
import com.example.tasksapi.dto.StartPomodoroTimerRequestDTO;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pomodoro/state")
public class PomodoroTimerStateController {
    private final PomodoroService pomodoroService;

    public PomodoroTimerStateController(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<PomodoroTimerStateDTO>> getState() {
        PomodoroTimerStateDTO data = pomodoroService.getPomodoroTimerState();
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Pomodoro timer state", data));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponseDTO<PomodoroTimerStateDTO>> start(
            @RequestBody(required = false) StartPomodoroTimerRequestDTO request) {
        PomodoroTimerStateDTO data = pomodoroService.startTimer(request);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Pomodoro timer started", data));
    }

    @PostMapping("/pause")
    public ResponseEntity<ApiResponseDTO<PomodoroTimerStateDTO>> pause() {
        PomodoroTimerStateDTO data = pomodoroService.pauseTimer();
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Pomodoro timer paused", data));
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponseDTO<PomodoroTimerStateDTO>> resume() {
        PomodoroTimerStateDTO data = pomodoroService.resumeTimer();
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Pomodoro timer resumed", data));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponseDTO<PomodoroTimerStateDTO>> reset(
            @RequestBody(required = false) ResetPomodoroTimerRequestDTO request) {
        PomodoroTimerStateDTO data = pomodoroService.resetTimer(request);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Pomodoro timer reset", data));
    }

    @PostMapping("/acknowledge")
    public ResponseEntity<ApiResponseDTO<PomodoroTimerStateDTO>> acknowledge(
            @RequestBody(required = false) AcknowledgePomodoroAlarmRequestDTO request) {
        PomodoroTimerStateDTO data = pomodoroService.acknowledgeAlarm(request);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Pomodoro alarm acknowledged", data));
    }
}
