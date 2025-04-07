package com.example.tasksapi.service;

import com.example.tasksapi.auth.JwtService;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.UserDTO;
import com.example.tasksapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    private JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public User registerUser(User user) {
       return userRepository.save(user);
    }

    public boolean userExists(String username, String email) {
        boolean usernameExists = userRepository.existsByUsername(username);
        boolean emailExists = userRepository.existsByEmail(email);

        return usernameExists && emailExists;
    }

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User> extractEmailFromTokenAndReturnUser(String token){
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email);
    }
}
