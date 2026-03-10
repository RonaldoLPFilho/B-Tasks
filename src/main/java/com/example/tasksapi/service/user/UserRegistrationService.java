package com.example.tasksapi.service.user;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.UserDTO;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import com.example.tasksapi.service.task.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {
    private final UserService userService;
    private final PomodoroService pomodoroService;
    private final PasswordEncoder passwordEncoder;
    private final UserLanguagePreferenceService userLanguagePreferenceService;
    private final CategoryService categoryService;

    public UserRegistrationService(
            UserService userService,
            PomodoroService pomodoroService,
            PasswordEncoder passwordEncoder,
            UserLanguagePreferenceService userLanguagePreferenceService,
            CategoryService categoryService) {
        this.userService = userService;
        this.pomodoroService = pomodoroService;
        this.passwordEncoder = passwordEncoder;
        this.userLanguagePreferenceService = userLanguagePreferenceService;
        this.categoryService = categoryService;
    }

    @Transactional
    public boolean registerUser(UserDTO dto) {
        if(userService.userExists(dto.username(), dto.email())){
            return false;
        }

        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.username(), dto.email(), encryptedPassword);
        User savedUser = userService.registerUser(user);
        categoryService.createDefaultCategory(savedUser);
        pomodoroService.createDefaultPomodoroPreferences(savedUser);
        userLanguagePreferenceService.createDefaultLanguagePreferences(savedUser);

        return true;
    }
}
