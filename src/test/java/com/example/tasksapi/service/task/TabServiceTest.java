package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import com.example.tasksapi.service.user.UserService;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TabRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TabServiceTest {

    @Mock
    private TabRepository tabRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private TabService service;

    @Test
    void shouldArchiveTabWithoutChangingDirectStateOfChildren() {
        UUID userId = UUID.randomUUID();
        UUID tabId = UUID.randomUUID();
        User user = withId(new User("ronis", "ronis@example.com", "encoded"), userId);
        Tab tab = new Tab();
        setField(tab, "id", tabId);
        tab.setUser(user);

        Section section = new Section();
        section.setName("Backlog");
        section.setTab(tab);
        Task task = new Task();
        task.setArchived(false);
        section.setTasks(new ArrayList<>(List.of(task)));
        tab.setSections(new ArrayList<>(List.of(section)));

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(tabRepository.findById(tabId)).thenReturn(Optional.of(tab));
        when(tabRepository.save(any(Tab.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.archive(tabId);

        assertTrue(tab.isArchived());
        assertFalse(section.isArchived());
        assertFalse(task.isArchived());
        verify(tabRepository).save(tab);
    }

    @Test
    void shouldUnarchiveTabWithoutChangingDirectStateOfChildren() {
        UUID userId = UUID.randomUUID();
        UUID tabId = UUID.randomUUID();
        User user = withId(new User("ronis", "ronis@example.com", "encoded"), userId);
        Tab tab = new Tab();
        setField(tab, "id", tabId);
        tab.setUser(user);
        tab.setArchived(true);

        Section section = new Section();
        section.setName("Backlog");
        section.setTab(tab);
        section.setArchived(true);
        Task task = new Task();
        task.setArchived(true);
        section.setTasks(new ArrayList<>(List.of(task)));
        tab.setSections(new ArrayList<>(List.of(section)));

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(tabRepository.findById(tabId)).thenReturn(Optional.of(tab));
        when(tabRepository.countByUserIdAndArchivedFalse(userId)).thenReturn(0L);
        when(tabRepository.save(any(Tab.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.unarchive(tabId);

        assertFalse(tab.isArchived());
        assertTrue(section.isArchived());
        assertTrue(task.isArchived());
        verify(tabRepository).save(tab);
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
