package com.example.tasksapi.controller;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.VaultItemRequestDTO;
import com.example.tasksapi.dto.VaultItemResponseDTO;
import com.example.tasksapi.dto.VaultUnlockRequestDTO;
import com.example.tasksapi.dto.VaultUnlockResponseDTO;
import com.example.tasksapi.service.vault.VaultItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/vault", produces = MediaType.APPLICATION_JSON_VALUE)
public class VaultController {
    private static final String VAULT_TOKEN_HEADER = "X-Vault-Token";

    private final VaultItemService vaultItemService;

    public VaultController(VaultItemService vaultItemService) {
        this.vaultItemService = vaultItemService;
    }

    @PostMapping("/unlock")
    public ResponseEntity<ApiResponseDTO<VaultUnlockResponseDTO>> unlock(@RequestBody VaultUnlockRequestDTO dto) {
        VaultUnlockResponseDTO data = vaultItemService.unlock(dto != null ? dto.password() : null);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Vault unlocked", data));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponseDTO<List<VaultItemResponseDTO>>> findAll(
            @RequestHeader(value = VAULT_TOKEN_HEADER, required = false) String vaultToken) {
        List<VaultItemResponseDTO> data = vaultItemService.findAll(vaultToken);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Vault items", data));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponseDTO<VaultItemResponseDTO>> findById(
            @PathVariable UUID id,
            @RequestHeader(value = VAULT_TOKEN_HEADER, required = false) String vaultToken) {
        VaultItemResponseDTO data = vaultItemService.findById(id, vaultToken);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Vault item found", data));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponseDTO<VaultItemResponseDTO>> create(
            @RequestBody VaultItemRequestDTO dto,
            @RequestHeader(value = VAULT_TOKEN_HEADER, required = false) String vaultToken) {
        VaultItemResponseDTO data = vaultItemService.create(dto, vaultToken);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Vault item created", data));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponseDTO<VaultItemResponseDTO>> update(
            @PathVariable UUID id,
            @RequestBody VaultItemRequestDTO dto,
            @RequestHeader(value = VAULT_TOKEN_HEADER, required = false) String vaultToken) {
        VaultItemResponseDTO data = vaultItemService.update(id, dto, vaultToken);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Vault item updated", data));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = VAULT_TOKEN_HEADER, required = false) String vaultToken) {
        vaultItemService.delete(id, vaultToken);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Vault item deleted", null));
    }
}
