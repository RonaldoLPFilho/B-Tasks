package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.dto.CreateSectionDTO;
import com.example.tasksapi.dto.UpdateSectionDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import com.example.tasksapi.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SectionService {

    private static final int MAX_SECTIONS_PER_TAB = 5;
    public static final String DEFAULT_SECTION_NAME = "Geral";

    private final SectionRepository sectionRepository;
    private final TaskRepository taskRepository;
    private final TabService tabService;
    private final UserService userService;
    private final AuthenticatedUserService authenticatedUserService;

    public SectionService(SectionRepository sectionRepository, TaskRepository taskRepository,
                          TabService tabService, UserService userService,
                          AuthenticatedUserService authenticatedUserService) {
        this.sectionRepository = sectionRepository;
        this.taskRepository = taskRepository;
        this.tabService = tabService;
        this.userService = userService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<Section> findByTabIdForCurrentUser(UUID tabId) {
        return findByTabId(tabId, authenticatedUserService.getCurrentUser().getId());
    }

    public List<Section> findByTabIdIncludingArchivedForCurrentUser(UUID tabId) {
        return findByTabIdIncludingArchived(tabId, authenticatedUserService.getCurrentUser().getId());
    }

    public List<Section> findByTabId(UUID tabId, String token) {
        return findByTabId(tabId, userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found")).getId());
    }

    public List<Section> findByTabId(UUID tabId, UUID userId) {
        tabService.findByIdAndValidateOwnership(tabId, userId);
        return sectionRepository.findActiveByTabIdOrderBySortOrderAsc(tabId);
    }

    public List<Section> findByTabIdIncludingArchived(UUID tabId, UUID userId) {
        tabService.findByIdAndValidateOwnership(tabId, userId);
        return sectionRepository.findByTabIdOrderBySortOrderAsc(tabId);
    }

    public Section findById(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Section not found with id " + id));
    }

    public Section findGeralSectionByTabId(UUID tabId, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return findGeralSectionByTabId(tabId, user.getId());
    }

    public Section findGeralSectionByTabId(UUID tabId, UUID userId) {
        tabService.findByIdAndValidateOwnership(tabId, userId);
        return sectionRepository.findByTabIdAndNameIgnoreCase(tabId, DEFAULT_SECTION_NAME)
                .orElseThrow(() -> new InvalidDataException("Default section 'Geral' not found for tab"));
    }

    public Section findByIdAndValidateTab(UUID sectionId, UUID tabId, UUID userId) {
        Section section = findById(sectionId);
        if (!section.getTab().getId().equals(tabId)) {
            throw new NotFoundException("Section not found with id " + sectionId);
        }
        tabService.findByIdAndValidateOwnership(tabId, userId);
        return section;
    }

    @Transactional
    public Section create(UUID tabId, CreateSectionDTO dto) {
        User user = authenticatedUserService.getCurrentUser();
        return createForUser(tabId, dto, user);
    }

    @Transactional
    public Section create(UUID tabId, CreateSectionDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return createForUser(tabId, dto, user);
    }

    private Section createForUser(UUID tabId, CreateSectionDTO dto, User user) {
        Tab tab = tabService.findByIdAndValidateOwnership(tabId, user.getId());

        if (dto.name() == null || dto.name().isBlank()) {
            throw new InvalidDataException("Section name is required");
        }

        if (tab.isArchived()) {
            throw new InvalidDataException("Cannot create sections in an archived tab");
        }

        long count = sectionRepository.countActiveByTabId(tabId);
        if (count >= MAX_SECTIONS_PER_TAB) {
            throw new InvalidDataException("Maximum of " + MAX_SECTIONS_PER_TAB + " sections per tab allowed");
        }

        int sortOrder = (int) count;
        Section section = new Section(dto.name().trim(), tab, sortOrder);
        section = sectionRepository.save(section);
        tab.getSections().add(section);
        return section;
    }

    @Transactional
    public Section update(UUID tabId, UUID sectionId, UpdateSectionDTO dto) {
        User user = authenticatedUserService.getCurrentUser();
        return updateForUser(tabId, sectionId, dto, user);
    }

    @Transactional
    public Section update(UUID tabId, UUID sectionId, UpdateSectionDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return updateForUser(tabId, sectionId, dto, user);
    }

    private Section updateForUser(UUID tabId, UUID sectionId, UpdateSectionDTO dto, User user) {
        Section section = findByIdAndValidateTab(sectionId, tabId, user.getId());

        if (DEFAULT_SECTION_NAME.equalsIgnoreCase(section.getName())) {
            throw new InvalidDataException("Cannot modify the default 'Geral' section");
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            section.setName(dto.name().trim());
        }

        if (dto.archived() != null) {
            if (dto.archived()) {
                archiveForUser(tabId, sectionId, user);
            } else {
                unarchiveForUser(tabId, sectionId, user);
            }
            return findByIdAndValidateTab(sectionId, tabId, user.getId());
        }

        return sectionRepository.save(section);
    }

    @Transactional
    public void archive(UUID tabId, UUID sectionId) {
        archiveForUser(tabId, sectionId, authenticatedUserService.getCurrentUser());
    }

    @Transactional
    public void unarchive(UUID tabId, UUID sectionId) {
        unarchiveForUser(tabId, sectionId, authenticatedUserService.getCurrentUser());
    }

    @Transactional
    public void delete(UUID tabId, UUID sectionId) {
        User user = authenticatedUserService.getCurrentUser();
        deleteForUser(tabId, sectionId, user);
    }

    @Transactional
    public void delete(UUID tabId, UUID sectionId, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        deleteForUser(tabId, sectionId, user);
    }

    private void deleteForUser(UUID tabId, UUID sectionId, User user) {
        Section section = findByIdAndValidateTab(sectionId, tabId, user.getId());

        Section geralSection = sectionRepository.findByTabIdAndNameIgnoreCase(tabId, DEFAULT_SECTION_NAME)
                .orElseThrow(() -> new InvalidDataException("Default section 'Geral' not found"));

        if (section.getId().equals(geralSection.getId())) {
            throw new InvalidDataException("Cannot delete the default 'Geral' section");
        }

        List<Task> tasks = section.getTasks();
        int nextOrder = taskRepository.findMaxSortOrderBySectionId(geralSection.getId()) + 1;
        for (Task task : tasks) {
            task.setSection(geralSection);
            task.setSortOrder(nextOrder++);
            taskRepository.save(task);
        }

        section.getTasks().clear();
        section.getTab().getSections().remove(section);
        sectionRepository.delete(section);
    }

    @Transactional
    public void reorder(UUID tabId, List<UUID> orderedIds) {
        User user = authenticatedUserService.getCurrentUser();
        reorderForUser(tabId, orderedIds, user);
    }

    @Transactional
    public void reorder(UUID tabId, List<UUID> orderedIds, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        reorderForUser(tabId, orderedIds, user);
    }

    private void reorderForUser(UUID tabId, List<UUID> orderedIds, User user) {
        tabService.findByIdAndValidateOwnership(tabId, user.getId());

        List<Section> sections = sectionRepository.findActiveByTabIdOrderBySortOrderAsc(tabId);
        Set<UUID> validIds = new HashSet<>(sections.stream().map(Section::getId).toList());
        if (orderedIds.size() != validIds.size() || !validIds.equals(new HashSet<>(orderedIds))) {
            throw new InvalidDataException("IDs don't belong to tab or are duplicated");
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            sectionRepository.updateSortOrder(orderedIds.get(i), i);
        }
    }

    private void archiveForUser(UUID tabId, UUID sectionId, User user) {
        Section section = findByIdAndValidateTab(sectionId, tabId, user.getId());
        if (DEFAULT_SECTION_NAME.equalsIgnoreCase(section.getName())) {
            throw new InvalidDataException("Cannot archive the default 'Geral' section");
        }
        if (section.isArchived()) {
            return;
        }

        section.setArchived(true);
        sectionRepository.save(section);
    }

    private void unarchiveForUser(UUID tabId, UUID sectionId, User user) {
        Section section = findByIdAndValidateTab(sectionId, tabId, user.getId());
        Tab tab = section.getTab();
        if (tab.isArchived()) {
            throw new InvalidDataException("Cannot unarchive a section while its tab is archived");
        }
        if (!section.isArchived()) {
            return;
        }

        section.setArchived(false);
        sectionRepository.save(section);
    }
}
