package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import com.example.tasksapi.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private TabService tabService;
    @Mock
    private SectionService sectionService;

    @InjectMocks
    private TaskService service;

    @Test
    void shouldShiftExistingTasksSafelyBeforeCreatingNewTaskAtTop() {
        User user = userWithId();
        UUID tabId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();

        Tab tab = new Tab();
        setField(tab, "id", tabId);
        tab.setUser(user);
        tab.setArchived(false);

        Section geral = new Section();
        setField(geral, "id", sectionId);
        geral.setTab(tab);
        geral.setName(SectionService.DEFAULT_SECTION_NAME);

        Task existingFirst = new Task();
        setField(existingFirst, "id", UUID.randomUUID());
        existingFirst.setSection(geral);
        existingFirst.setTab(tab);
        existingFirst.setUser(user);
        existingFirst.setSortOrder(0);

        Task existingSecond = new Task();
        setField(existingSecond, "id", UUID.randomUUID());
        existingSecond.setSection(geral);
        existingSecond.setTab(tab);
        existingSecond.setUser(user);
        existingSecond.setSortOrder(1);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("Nova task");
        dto.setDescription("Descricao");
        dto.setTabId(tabId);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(tabService.findByIdAndValidateOwnership(tabId, user.getId())).thenReturn(tab);
        when(sectionService.findGeralSectionByTabId(tabId, user.getId())).thenReturn(geral);
        when(categoryService.ensureDefaultCategory(user)).thenReturn(new Category());
        when(taskRepository.findBySection_IdOrderBySortOrderAsc(sectionId))
                .thenReturn(new ArrayList<>(List.of(existingFirst, existingSecond)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(dto);

        assertEquals(1, existingFirst.getSortOrder());
        assertEquals(2, existingSecond.getSortOrder());
        verify(taskRepository, times(2)).saveAll(any());
        verify(taskRepository, times(2)).flush();

        ArgumentCaptor<Task> createdTaskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(createdTaskCaptor.capture());
        assertEquals(0, createdTaskCaptor.getValue().getSortOrder());
        assertEquals(geral, createdTaskCaptor.getValue().getSection());
    }

    @Test
    void shouldAppendTaskToNextAvailableOrderWhenUnarchiving() {
        User user = userWithId();
        UUID taskId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID tabId = UUID.randomUUID();

        Tab tab = new Tab();
        setField(tab, "id", tabId);
        tab.setUser(user);
        tab.setArchived(false);

        Section section = new Section();
        setField(section, "id", sectionId);
        section.setTab(tab);
        section.setArchived(false);

        Task task = new Task();
        setField(task, "id", taskId);
        task.setUser(user);
        task.setSection(section);
        task.setTab(tab);
        task.setArchived(true);
        task.setSortOrder(1);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.findMaxSortOrderBySectionId(sectionId)).thenReturn(4);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.unarchiveTask(taskId);

        assertFalse(task.isArchived());
        assertEquals(5, task.getSortOrder());
        verify(taskRepository).save(task);
    }

    private User userWithId() {
        User user = new User("ronis", "ronis@example.com", "encoded");
        setField(user, "id", UUID.randomUUID());
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
