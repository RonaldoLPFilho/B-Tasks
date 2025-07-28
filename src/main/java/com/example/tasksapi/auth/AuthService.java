package com.example.tasksapi.auth;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.LoginDTO;
import com.example.tasksapi.dto.LoginResponseDTO;
import com.example.tasksapi.dto.UserDTO;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.exception.UnauthorizedException;
import com.example.tasksapi.service.user.UserRegistrationService;
import com.example.tasksapi.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRegistrationService userRegistrationService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService, UserRegistrationService userRegistrationService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRegistrationService = userRegistrationService;
    }

    public boolean register(UserDTO dto) {
        return userRegistrationService.registerUser(dto);
    }

    public LoginResponseDTO login(LoginDTO dto) {
        Optional<User> userOptional = userService.findByEmail(dto.email());

        if(userOptional.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new UnauthorizedException("Credentials wrong");
        }

        LoginResponseDTO responseDTO = new LoginResponseDTO();

        responseDTO.setToken(jwtService.generateToken(user));
        responseDTO.setUsername(user.getUsername());

        return responseDTO;
    }
}
