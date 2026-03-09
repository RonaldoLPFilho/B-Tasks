package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.CreateTabDTO;
import com.example.tasksapi.dto.UpdateTabDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.exception.UnauthorizedException;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TabRepository;
import com.example.tasksapi.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TabService {

    private static final int MAX_ACTIVE_TABS = 5;
    private static final int MAX_TAB_NAME_LENGTH = 20;

    private static final String DEFAULT_SECTION_NAME = "Geral";

    private final TabRepository tabRepository;
    private final SectionRepository sectionRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public TabService(TabRepository tabRepository, SectionRepository sectionRepository,
                      UserService userService, PasswordEncoder passwordEncoder) {
        this.tabRepository = tabRepository;
        this.sectionRepository = sectionRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Tab> findAllByToken(String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return tabRepository.findByUserIdAndArchivedFalseOrderBySortOrderAsc(user.getId());
    }

    public List<Tab> findAllIncludingArchived(String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return tabRepository.findByUserIdOrderBySortOrderAsc(user.getId());
    }

    public Tab findById(UUID id) {
        Tab tab = tabRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tab not found with id " + id));
        return tab;
    }

    public Tab findByIdAndValidateOwnership(UUID id, UUID userId) {
        Tab tab = findById(id);
        if (!tab.getUser().getId().equals(userId)) {
            throw new NotFoundException("Tab not found with id " + id);
        }
        return tab;
    }

    @Transactional
    public Tab create(CreateTabDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (dto.name() == null || dto.name().isBlank()) {
            throw new InvalidDataException("Tab name is required");
        }
        String trimmedName = dto.name().trim();
        if (trimmedName.length() > MAX_TAB_NAME_LENGTH) {
            throw new InvalidDataException("Tab name must have at most " + MAX_TAB_NAME_LENGTH + " characters");
        }

        long activeCount = tabRepository.countByUserIdAndArchivedFalse(user.getId());
        if (activeCount >= MAX_ACTIVE_TABS) {
            throw new InvalidDataException("Maximum of " + MAX_ACTIVE_TABS + " active tabs allowed. Archive or remove a tab first.");
        }

        Tab tab = new Tab();
        tab.setName(trimmedName);
        tab.setUser(user);
        tab.setArchived(false);
        tab.setSortOrder((int) activeCount);
        tab = tabRepository.save(tab);

        Section geralSection = new Section(DEFAULT_SECTION_NAME, tab, 0);
        sectionRepository.save(geralSection);
        tab.getSections().add(geralSection);

        return tab;
    }

    @Transactional
    public Tab update(UUID id, UpdateTabDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Tab tab = findByIdAndValidateOwnership(id, user.getId());

        if (dto.name() != null && !dto.name().isBlank()) {
            String trimmedName = dto.name().trim();
            if (trimmedName.length() > MAX_TAB_NAME_LENGTH) {
                throw new InvalidDataException("Tab name must have at most " + MAX_TAB_NAME_LENGTH + " characters");
            }
            tab.setName(trimmedName);
        }

        if (dto.archived() != null) {
            if (dto.archived()) {
                tab.setArchived(true);
            } else {
                long activeCount = tabRepository.countByUserIdAndArchivedFalse(user.getId());
                if (activeCount >= MAX_ACTIVE_TABS) {
                    throw new InvalidDataException("Maximum of " + MAX_ACTIVE_TABS + " active tabs allowed. Archive another tab first.");
                }
                tab.setArchived(false);
            }
        }

        return tabRepository.save(tab);
    }

    @Transactional
    public void archive(UUID id, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Tab tab = findByIdAndValidateOwnership(id, user.getId());
        tab.setArchived(true);
        tabRepository.save(tab);
    }

    @Transactional
    public void unarchive(UUID id, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Tab tab = findByIdAndValidateOwnership(id, user.getId());

        long activeCount = tabRepository.countByUserIdAndArchivedFalse(user.getId());
        if (activeCount >= MAX_ACTIVE_TABS) {
            throw new InvalidDataException("Maximum of " + MAX_ACTIVE_TABS + " active tabs allowed. Archive another tab first.");
        }

        tab.setArchived(false);
        tabRepository.save(tab);
    }

    @Transactional
    public void delete(UUID id, String token, String password) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Tab tab = findByIdAndValidateOwnership(id, user.getId());

        boolean hasTasks = tab.hasTasks();
        if (hasTasks) {
            if (password == null || password.isBlank()) {
                throw new InvalidDataException("Password confirmation is required to remove a tab that contains tasks");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new UnauthorizedException("Invalid password");
            }
        }

        tabRepository.delete(tab);
    }

    public boolean existsByIdAndUserId(UUID id, UUID userId) {
        return tabRepository.existsByIdAndUserId(id, userId);
    }
}
