package com.example.tasksapi.service.user;

import com.example.tasksapi.auth.JwtService;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.repository.UserRepository;
import com.example.tasksapi.utils.UserUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final JwtService jwtService;


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

    public String extractEmailFromContext(){
        User user = UserUtils.getCurrentUser();
        return user.getEmail();
    }
}
