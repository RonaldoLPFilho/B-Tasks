package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.ArchivedItemTypeDTO;
import com.example.tasksapi.dto.ArchivedSearchResultDTO;
import com.example.tasksapi.dto.RestoreSectionRequestDTO;
import com.example.tasksapi.dto.RestoreTaskRequestDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TabRepository;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveServiceTest {

    @Mock
    private TabRepository tabRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TabService tabService;
    @Mock
    private SectionService sectionService;
    @Mock
    private TaskService taskService;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Spy
    private TaskResponseMapper taskResponseMapper;

    @InjectMocks
    private ArchiveService archiveService;

    @Test
    void shouldReturnTypedArchivedResultsForMatchingTab() {
        User user = userWithId();
        Tab tab = archivedTabWithHierarchy(user, "Platform");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(tabRepository.findByUserIdAndArchivedTrueOrderBySortOrderAsc(user.getId())).thenReturn(List.of(tab));
        when(sectionRepository.findArchivedForUser(user.getId())).thenReturn(List.of());
        when(taskRepository.findArchivedForUser(user.getId())).thenReturn(List.of());

        List<ArchivedSearchResultDTO> results = archiveService.searchArchived("platform", 10);

        assertEquals(1, results.size());
        ArchivedSearchResultDTO result = results.get(0);
        assertEquals(ArchivedItemTypeDTO.TAB, result.type());
        assertEquals(tab.getId(), result.id());
        assertFalse(result.matches().isEmpty());
        assertTrue(result.parentTabArchived());
    }

    @Test
    void shouldRestoreSectionWithParentsWhenRequested() {
        User user = userWithId();
        Tab archivedTab = new Tab();
        setField(archivedTab, "id", UUID.randomUUID());
        archivedTab.setUser(user);
        archivedTab.setArchived(true);
        archivedTab.setName("Arquivada");

        Section section = new Section();
        setField(section, "id", UUID.randomUUID());
        section.setName("Backlog");
        section.setTab(archivedTab);
        section.setArchived(true);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(sectionService.findById(section.getId())).thenReturn(section);
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> invocation.getArgument(0));

        archiveService.restoreSection(section.getId(), new RestoreSectionRequestDTO(true, null));

        verify(tabService).unarchive(archivedTab.getId());
        assertFalse(section.isArchived());
        verify(sectionRepository).save(section);
    }

    @Test
    void shouldRequireManualTargetWhenTaskOriginIsStillArchived() {
        User user = userWithId();
        Tab archivedTab = new Tab();
        setField(archivedTab, "id", UUID.randomUUID());
        archivedTab.setUser(user);
        archivedTab.setArchived(true);

        Section archivedSection = new Section();
        setField(archivedSection, "id", UUID.randomUUID());
        archivedSection.setTab(archivedTab);
        archivedSection.setArchived(false);

        Task task = new Task();
        setField(task, "id", UUID.randomUUID());
        task.setSection(archivedSection);
        task.setTab(archivedTab);
        task.setArchived(true);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(taskService.findByIdAndValidateOwnership(task.getId(), user.getId())).thenReturn(task);

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> archiveService.restoreTask(task.getId(), new RestoreTaskRequestDTO(false, null, null))
        );

        assertEquals("Target tab and section are required when origin is unavailable", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldMoveTaskToChosenDestinationWhenUserSelectsNewLocation() {
        User user = userWithId();

        Tab archivedTab = new Tab();
        setField(archivedTab, "id", UUID.randomUUID());
        archivedTab.setUser(user);
        archivedTab.setArchived(true);

        Section archivedSection = new Section();
        setField(archivedSection, "id", UUID.randomUUID());
        archivedSection.setTab(archivedTab);
        archivedSection.setArchived(true);

        Task task = new Task();
        setField(task, "id", UUID.randomUUID());
        task.setSection(archivedSection);
        task.setTab(archivedTab);
        task.setArchived(true);

        Tab activeTab = new Tab();
        setField(activeTab, "id", UUID.randomUUID());
        activeTab.setUser(user);
        activeTab.setArchived(false);
        activeTab.setName("Ativa");

        Section targetSection = new Section();
        setField(targetSection, "id", UUID.randomUUID());
        targetSection.setTab(activeTab);
        targetSection.setArchived(false);
        targetSection.setTasks(new ArrayList<>(List.of(new Task(), new Task())));

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(taskService.findByIdAndValidateOwnership(task.getId(), user.getId())).thenReturn(task);
        when(tabService.findByIdAndValidateOwnership(activeTab.getId(), user.getId())).thenReturn(activeTab);
        when(sectionService.findByIdAndValidateTab(targetSection.getId(), activeTab.getId(), user.getId())).thenReturn(targetSection);
        when(taskRepository.findMaxSortOrderBySectionId(targetSection.getId())).thenReturn(4);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        archiveService.restoreTask(
                task.getId(),
                new RestoreTaskRequestDTO(false, activeTab.getId(), targetSection.getId())
        );

        ArgumentCaptor<Task> savedTask = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(savedTask.capture());

        assertEquals(activeTab, savedTask.getValue().getTab());
        assertEquals(targetSection, savedTask.getValue().getSection());
        assertFalse(savedTask.getValue().isArchived());
        assertEquals(5, savedTask.getValue().getSortOrder());
    }

    private User userWithId() {
        User user = new User("ronis", "ronis@example.com", "encoded");
        setField(user, "id", UUID.randomUUID());
        return user;
    }

    private Tab archivedTabWithHierarchy(User user, String tabName) {
        Tab tab = new Tab();
        setField(tab, "id", UUID.randomUUID());
        tab.setUser(user);
        tab.setName(tabName);
        tab.setArchived(true);

        Section section = new Section();
        setField(section, "id", UUID.randomUUID());
        section.setName("Docs");
        section.setTab(tab);
        section.setArchived(false);

        Task task = new Task();
        setField(task, "id", UUID.randomUUID());
        task.setTitle("Revisar API");
        task.setDescription("Conferir comportamento da busca");
        task.setSection(section);
        task.setTab(tab);
        task.setArchived(false);

        section.setTasks(new ArrayList<>(List.of(task)));
        tab.setSections(new ArrayList<>(List.of(section)));
        return tab;
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
