package com.example.tasksapi.controller;

import com.example.tasksapi.dto.VaultEntryDTO;
import com.example.tasksapi.dto.VaultItemRequestDTO;
import com.example.tasksapi.dto.VaultItemResponseDTO;
import com.example.tasksapi.dto.VaultUnlockRequestDTO;
import com.example.tasksapi.dto.VaultUnlockResponseDTO;
import com.example.tasksapi.service.vault.VaultItemService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VaultControllerTest {
    @Test
    void shouldUnlockUsingPassword() {
        VaultItemService service = mock(VaultItemService.class);
        VaultController controller = new VaultController(service);
        VaultUnlockResponseDTO unlockResponse = new VaultUnlockResponseDTO("vault-token", Instant.now());
        when(service.unlock("plain")).thenReturn(unlockResponse);

        var response = controller.unlock(new VaultUnlockRequestDTO("plain"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(unlockResponse, response.getBody().getData());
        verify(service).unlock("plain");
    }

    @Test
    void shouldPassVaultTokenHeaderToCreate() {
        VaultItemService service = mock(VaultItemService.class);
        VaultController controller = new VaultController(service);
        VaultItemRequestDTO request = new VaultItemRequestDTO("GitHub", null, List.of(new VaultEntryDTO("token", "secret")));
        VaultItemResponseDTO serviceResponse = new VaultItemResponseDTO(UUID.randomUUID(), "GitHub", null, request.entries(), null, null);
        when(service.create(request, "vault-token")).thenReturn(serviceResponse);

        var response = controller.create(request, "vault-token");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody().getData());
        verify(service).create(request, "vault-token");
    }
}
