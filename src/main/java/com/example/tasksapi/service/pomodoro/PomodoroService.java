package com.example.tasksapi.service.pomodoro;

import com.example.tasksapi.domain.pomodoro.PomodoroPreferences;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.CreatePomodoroPreferenceDTO;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.PomodoroPreferencesRepository;
import com.example.tasksapi.service.user.UserService;
import com.example.tasksapi.utils.UserUtils;
import org.springframework.stereotype.Service;


@Service
public class PomodoroService {
    private final PomodoroPreferencesRepository pomodoroPreferencesRepository;
    private final UserService userService;

    public PomodoroService(PomodoroPreferencesRepository pomodoroPreferencesRepository, UserService userService) {
        this.pomodoroPreferencesRepository = pomodoroPreferencesRepository;
        this.userService = userService;
    }

    public void createDefaultPomodoroPreferences(User user) {
        PomodoroPreferences pomodoroPreferences = new PomodoroPreferences(
                user,
                30,
                5,
                "aplausos.wav"

        );

        pomodoroPreferencesRepository.save(pomodoroPreferences);
    }

    public PomodoroPreferences getPomodoroPreferences() {
        User user = UserUtils.getCurrentUser();

        return pomodoroPreferencesRepository.findByUserId(user.getId());
    }

    public PomodoroPreferences updatePomodoroPreferences(CreatePomodoroPreferenceDTO dto) {
        User user = UserUtils.getCurrentUser();

        PomodoroPreferences preferences = new PomodoroPreferences(
                user,
                dto.sessionDuration(),
                dto.breakDuration(),
                dto.alarmSound()
        );

        return pomodoroPreferencesRepository.save(preferences);
    }

}
