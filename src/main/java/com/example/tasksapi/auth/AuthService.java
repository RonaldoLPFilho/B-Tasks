package com.example.tasksapi.auth;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.LoginDTO;
import com.example.tasksapi.dto.UserDTO;
import com.example.tasksapi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public String login(LoginDTO dto) {
        Optional<User> userOptional = userService.findByEmail(dto.email());

        if(userOptional.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Senha incorreta");
        }

        return jwtService.generateToken(user);
    }

}
