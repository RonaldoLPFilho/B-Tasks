package com.example.tasksapi.service.vault;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.vault.VaultEntry;
import com.example.tasksapi.domain.vault.VaultItem;
import com.example.tasksapi.dto.VaultEntryDTO;
import com.example.tasksapi.dto.VaultItemRequestDTO;
import com.example.tasksapi.dto.VaultItemResponseDTO;
import com.example.tasksapi.dto.VaultUnlockResponseDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.exception.UnauthorizedException;
import com.example.tasksapi.repository.VaultItemRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VaultItemService {
    private final VaultItemRepository vaultItemRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final PasswordEncoder passwordEncoder;
    private final VaultCryptoService cryptoService;
    private final VaultTokenService tokenService;

    public VaultItemService(VaultItemRepository vaultItemRepository,
                            AuthenticatedUserService authenticatedUserService,
                            PasswordEncoder passwordEncoder,
                            VaultCryptoService cryptoService,
                            VaultTokenService tokenService) {
        this.vaultItemRepository = vaultItemRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.passwordEncoder = passwordEncoder;
        this.cryptoService = cryptoService;
        this.tokenService = tokenService;
    }

    public VaultUnlockResponseDTO unlock(String password) {
        User user = authenticatedUserService.getCurrentUser();
        if (password == null || password.isBlank()) {
            throw new InvalidDataException("Password is required");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }
        return tokenService.issue(user);
    }

    public List<VaultItemResponseDTO> findAll(String vaultToken) {
        User user = authenticatedUserService.getCurrentUser();
        tokenService.validate(vaultToken, user);
        return vaultItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VaultItemResponseDTO findById(UUID id, String vaultToken) {
        User user = authenticatedUserService.getCurrentUser();
        tokenService.validate(vaultToken, user);
        return toResponse(findByIdAndValidateOwnership(id, user.getId()));
    }

    @Transactional
    public VaultItemResponseDTO create(VaultItemRequestDTO dto, String vaultToken) {
        User user = authenticatedUserService.getCurrentUser();
        tokenService.validate(vaultToken, user);
        validateRequest(dto);

        VaultItem item = new VaultItem();
        item.setUser(user);
        applyRequest(item, dto);
        return toResponse(vaultItemRepository.save(item));
    }

    @Transactional
    public VaultItemResponseDTO update(UUID id, VaultItemRequestDTO dto, String vaultToken) {
        User user = authenticatedUserService.getCurrentUser();
        tokenService.validate(vaultToken, user);
        validateRequest(dto);

        VaultItem item = findByIdAndValidateOwnership(id, user.getId());
        applyRequest(item, dto);
        return toResponse(vaultItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id, String vaultToken) {
        User user = authenticatedUserService.getCurrentUser();
        tokenService.validate(vaultToken, user);
        vaultItemRepository.delete(findByIdAndValidateOwnership(id, user.getId()));
    }

    private VaultItem findByIdAndValidateOwnership(UUID id, UUID userId) {
        VaultItem item = vaultItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vault item not found with id " + id));
        if (!item.getUser().getId().equals(userId)) {
            throw new NotFoundException("Vault item not found with id " + id);
        }
        return item;
    }

    private void applyRequest(VaultItem item, VaultItemRequestDTO dto) {
        item.setEncryptedName(cryptoService.encrypt(dto.name().trim()));
        item.setEncryptedDescription(cryptoService.encrypt(blankToNull(dto.description())));
        item.getEntries().clear();

        int sortOrder = 0;
        for (VaultEntryDTO entryDTO : dto.entries()) {
            VaultEntry entry = new VaultEntry();
            entry.setEncryptedKey(cryptoService.encrypt(entryDTO.key().trim()));
            entry.setEncryptedValue(cryptoService.encrypt(entryDTO.value()));
            entry.setSortOrder(sortOrder++);
            entry.setItem(item);
            item.getEntries().add(entry);
        }
    }

    private VaultItemResponseDTO toResponse(VaultItem item) {
        return new VaultItemResponseDTO(
                item.getId(),
                cryptoService.decrypt(item.getEncryptedName()),
                cryptoService.decrypt(item.getEncryptedDescription()),
                item.getEntries().stream()
                        .map(entry -> new VaultEntryDTO(
                                cryptoService.decrypt(entry.getEncryptedKey()),
                                cryptoService.decrypt(entry.getEncryptedValue())
                        ))
                        .toList(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private void validateRequest(VaultItemRequestDTO dto) {
        if (dto == null) {
            throw new InvalidDataException("Vault item is required");
        }
        if (dto.name() == null || dto.name().isBlank()) {
            throw new InvalidDataException("Vault item name is required");
        }
        if (dto.entries() == null || dto.entries().isEmpty()) {
            throw new InvalidDataException("At least one vault entry is required");
        }
        for (VaultEntryDTO entry : dto.entries()) {
            if (entry == null || entry.key() == null || entry.key().isBlank()) {
                throw new InvalidDataException("Vault entry key is required");
            }
            if (entry.value() == null) {
                throw new InvalidDataException("Vault entry value is required");
            }
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
