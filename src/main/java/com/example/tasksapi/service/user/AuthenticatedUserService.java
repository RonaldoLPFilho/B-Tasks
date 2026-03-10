package com.example.tasksapi.service.user;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User user)) {
            throw new UnauthorizedException("Error to validate current user");
        }

        return user;
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}
