package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import com.example.tasksapi.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TabService tabService;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private SectionService service;

    @Test
    void shouldArchiveSectionWithoutChangingDirectStateOfTasks() {
        UUID userId = UUID.randomUUID();
        UUID tabId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        User user = withId(new User("ronis", "ronis@example.com", "encoded"), userId);

        Tab tab = new Tab();
        setField(tab, "id", tabId);
        tab.setUser(user);

        Section section = new Section();
        section.setName("Backlog");
        section.setTab(tab);
        setField(section, "id", sectionId);

        Task task = new Task();
        task.setArchived(false);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(tabService.findByIdAndValidateOwnership(tabId, userId)).thenReturn(tab);
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.archive(tabId, sectionId);

        assertTrue(section.isArchived());
        assertFalse(task.isArchived());
        verify(sectionRepository).save(section);
    }

    @Test
    void shouldRejectArchivingDefaultSection() {
        UUID userId = UUID.randomUUID();
        UUID tabId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        User user = withId(new User("ronis", "ronis@example.com", "encoded"), userId);

        Tab tab = new Tab();
        setField(tab, "id", tabId);
        tab.setUser(user);

        Section section = new Section();
        section.setName("Geral");
        section.setTab(tab);
        setField(section, "id", sectionId);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(tabService.findByIdAndValidateOwnership(tabId, userId)).thenReturn(tab);

        assertThrows(InvalidDataException.class, () -> service.archive(tabId, sectionId));
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
