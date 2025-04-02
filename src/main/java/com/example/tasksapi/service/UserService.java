//package com.example.tasksapi.service;
//
//import com.example.tasksapi.domain.User;
//import com.example.tasksapi.dto.UserDTO;
//import com.example.tasksapi.repository.UserRepository;
//import org.springframework.http.ResponseEntity;
//
//public class UserService {
//    private UserRepository userRepository;
//
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
////    public ResponseEntity<Boolean> createUser(UserDTO dto) {
////
////    }
//
//    public boolean userExists(String username, String email) {
//        boolean usernameExists =  userRepository.existsByEmail(email);
//        boolean emailExists =  userRepository.existsByUsername(username);
//
//        return usernameExists && emailExists;
//    }
//}
