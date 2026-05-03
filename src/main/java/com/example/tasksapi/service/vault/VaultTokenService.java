package com.example.tasksapi.service.vault;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.VaultUnlockResponseDTO;
import com.example.tasksapi.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class VaultTokenService {
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, VaultToken> tokens = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration tokenTtl;

    @Autowired
    public VaultTokenService(@Value("${tasks.vault.token-ttl-minutes:10}") long tokenTtlMinutes) {
        this(Clock.systemUTC(), Duration.ofMinutes(tokenTtlMinutes));
    }

    VaultTokenService(Clock clock, Duration tokenTtl) {
        this.clock = clock;
        this.tokenTtl = tokenTtl;
    }

    public VaultUnlockResponseDTO issue(User user) {
        cleanupExpiredTokens();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = clock.instant().plus(tokenTtl);
        tokens.put(token, new VaultToken(user.getId(), expiresAt));
        return new VaultUnlockResponseDTO(token, expiresAt);
    }

    public void validate(String token, User user) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Vault token is required");
        }

        VaultToken vaultToken = tokens.get(token);
        if (vaultToken == null || !vaultToken.userId().equals(user.getId())) {
            throw new UnauthorizedException("Invalid vault token");
        }

        if (vaultToken.expiresAt().isBefore(clock.instant())) {
            tokens.remove(token);
            throw new UnauthorizedException("Vault token expired");
        }
    }

    private void cleanupExpiredTokens() {
        Instant now = clock.instant();
        tokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record VaultToken(UUID userId, Instant expiresAt) {
    }
}
