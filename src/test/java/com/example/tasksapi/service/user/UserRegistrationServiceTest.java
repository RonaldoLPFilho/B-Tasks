package com.example.tasksapi.service.user;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.UserDTO;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import com.example.tasksapi.service.task.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PomodoroService pomodoroService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserLanguagePreferenceService userLanguagePreferenceService;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private UserRegistrationService service;

    @Test
    void shouldRejectRegistrationWhenUsernameOrEmailAlreadyExists() {
        UserDTO dto = new UserDTO("ronis", "ronis@example.com", "123456");
        when(userService.userExists(dto.username(), dto.email())).thenReturn(true);

        boolean created = service.registerUser(dto);

        assertFalse(created);
        verify(userService, never()).registerUser(any());
        verify(categoryService, never()).createDefaultCategory(any());
        verify(pomodoroService, never()).createDefaultPomodoroPreferences(any());
        verify(userLanguagePreferenceService, never()).createDefaultLanguagePreferences(any());
    }

    @Test
    void shouldCreateUserAndBootstrapDefaults() {
        UserDTO dto = new UserDTO("ronis", "ronis@example.com", "123456");
        User savedUser = new User("ronis", "ronis@example.com", "encoded-password");

        when(userService.userExists(dto.username(), dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");
        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        boolean created = service.registerUser(dto);

        assertTrue(created);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).registerUser(userCaptor.capture());
        assertEquals("ronis", userCaptor.getValue().getUsername());
        assertEquals("ronis@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());

        verify(categoryService).createDefaultCategory(savedUser);
        verify(pomodoroService).createDefaultPomodoroPreferences(savedUser);
        verify(userLanguagePreferenceService).createDefaultLanguagePreferences(savedUser);
    }
}
