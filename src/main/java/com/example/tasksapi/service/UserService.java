package com.example.tasksapi.service;

import com.example.tasksapi.auth.JwtService;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.repository.UserRepository;
import com.example.tasksapi.service.pomodoro.PomodoroService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    private JwtService jwtService;

    private PomodoroService pomodoroService;

    public UserService(UserRepository userRepository, JwtService jwtService, PomodoroService pomodoroService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.pomodoroService = pomodoroService;
    }

    public void registerUser(User user) {
        User savedUser =  userRepository.save(user);
        pomodoroService.createDefaultPomodoroPreferences(savedUser);
    }

    public boolean userExists(String username, String email) {
        boolean usernameExists = userRepository.existsByUsername(username);
        boolean emailExists = userRepository.existsByEmail(email);

        return usernameExists && emailExists;
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User> extractEmailFromTokenAndReturnUser(String token){
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email);
    }
}
