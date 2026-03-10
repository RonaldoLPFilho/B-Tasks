package com.example.tasksapi.auth;

import com.example.tasksapi.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-key-with-at-least-32-bytes-123456";

    @Test
    void shouldValidateTokenAcrossServiceInstancesWithSameSecret() {
        JwtService issuer = new JwtService(SECRET, 4);
        JwtService validator = new JwtService(SECRET, 4);

        User user = new User("ronis", "ronis@example.com", "encoded");
        String token = issuer.generateToken(user);

        assertEquals("ronis@example.com", validator.extractEmail(token));
        assertTrue(validator.isTokenValid(token, user));
    }
}
