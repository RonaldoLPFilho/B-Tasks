package com.example.tasksapi.service.vault;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.vault.VaultItem;
import com.example.tasksapi.dto.VaultEntryDTO;
import com.example.tasksapi.dto.VaultItemRequestDTO;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.exception.UnauthorizedException;
import com.example.tasksapi.repository.VaultItemRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultItemServiceTest {
    private static final String DEV_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Mock
    private VaultItemRepository vaultItemRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private VaultItemService service;
    private VaultTokenService tokenService;
    private User user;

    @BeforeEach
    void setUp() {
        tokenService = new VaultTokenService(java.time.Clock.systemUTC(), Duration.ofMinutes(10));
        service = new VaultItemService(
                vaultItemRepository,
                authenticatedUserService,
                passwordEncoder,
                new VaultCryptoService(DEV_KEY),
                tokenService
        );
        user = withId(new User("ronis", "ronis@example.com", "encoded"), UUID.randomUUID());
    }

    @Test
    void shouldUnlockWithPasswordAndReturnVaultToken() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);

        var response = service.unlock("plain");

        tokenService.validate(response.token(), user);
    }

    @Test
    void shouldRejectInvalidPasswordOnUnlock() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.unlock("wrong"));
    }

    @Test
    void shouldEncryptSensitiveFieldsBeforePersistingAndReturnDecryptedResponse() {
        String vaultToken = tokenService.issue(user).token();
        var request = new VaultItemRequestDTO(
                "GitHub",
                "Personal token",
                List.of(new VaultEntryDTO("token", "ghp_secret"))
        );
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(vaultItemRepository.save(any(VaultItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request, vaultToken);

        ArgumentCaptor<VaultItem> captor = ArgumentCaptor.forClass(VaultItem.class);
        verify(vaultItemRepository).save(captor.capture());
        VaultItem persisted = captor.getValue();

        assertNotEquals("GitHub", persisted.getEncryptedName());
        assertNotEquals("Personal token", persisted.getEncryptedDescription());
        assertNotEquals("token", persisted.getEntries().get(0).getEncryptedKey());
        assertNotEquals("ghp_secret", persisted.getEntries().get(0).getEncryptedValue());
        assertEquals("GitHub", response.name());
        assertEquals("Personal token", response.description());
        assertEquals("token", response.entries().get(0).key());
        assertEquals("ghp_secret", response.entries().get(0).value());
    }

    @Test
    void shouldBlockCrossUserAccess() {
        UUID itemId = UUID.randomUUID();
        String vaultToken = tokenService.issue(user).token();
        User otherUser = withId(new User("ana", "ana@example.com", "encoded"), UUID.randomUUID());
        VaultItem item = new VaultItem();
        item.setUser(otherUser);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(vaultItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> service.findById(itemId, vaultToken));
    }

    @Test
    void shouldRequireVaultTokenForCrud() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> service.findAll(null));
    }

    private User withId(User user, UUID id) {
        setField(user, "id", id);
        return user;
    }

    private void setField(Object target, String fieldName, Object value) {
        Class<?> current = target.getClass();

        while (current != null) {
            try {
                var field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalArgumentException("Field not found: " + fieldName);
    }
}
