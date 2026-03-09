package com.example.tasksapi.config;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.repository.UserRepository;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(4)
public class PomodoroPreferencesMigrationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PomodoroPreferencesMigrationRunner.class);

    private final UserRepository userRepository;
    private final PomodoroService pomodoroService;

    public PomodoroPreferencesMigrationRunner(UserRepository userRepository, PomodoroService pomodoroService) {
        this.userRepository = userRepository;
        this.pomodoroService = pomodoroService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int ensuredPreferences = 0;

        for (User user : userRepository.findAll()) {
            pomodoroService.createDefaultPomodoroPreferences(user);
            ensuredPreferences++;
        }

        if (ensuredPreferences > 0) {
            log.info("Ensured pomodoro preferences and timer state for {} users", ensuredPreferences);
        }
    }
}
