package com.example.tasksapi.controller.pomodoro;

import com.example.tasksapi.domain.pomodoro.PomodoroSoundOption;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.service.pomodoro.PomodoroAlarmSoundService;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pomodoro/sounds")
public class PomodoroSoundController {
    private final PomodoroAlarmSoundService pomodoroAlarmSoundService;

    public PomodoroSoundController(PomodoroAlarmSoundService pomodoroAlarmSoundService) {
        this.pomodoroAlarmSoundService = pomodoroAlarmSoundService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PomodoroSoundOption>>> getAvailableSoundOptions() {
        List<PomodoroSoundOption> sounds = pomodoroAlarmSoundService.listAvailableSounds();

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "sounds available", sounds));
    }

}
