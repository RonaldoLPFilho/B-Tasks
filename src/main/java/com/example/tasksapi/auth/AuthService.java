package com.example.tasksapi.auth;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.UserDTO;
import com.example.tasksapi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean register(UserDTO dto) {
        if(userService.userExists(dto.username(), dto.email())) {
            return false;
        }
        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.username(), dto.email(), encryptedPassword);
        userService.registerUser(user);
        return true;
    }
}
