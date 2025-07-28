package com.example.tasksapi.utils;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtils {
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            User user = (User) authentication.getPrincipal();
            return user.getUsername();
        }else{
            throw new UnauthorizedException("Error to validate current user");
        }
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            User user = (User) authentication.getPrincipal();
            return user;
        }else{
            throw new UnauthorizedException("Error to validate current user");
        }
    }
}
