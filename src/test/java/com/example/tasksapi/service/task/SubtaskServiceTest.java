package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Subtask;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.CreateSubstaskRequestDTO;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.SubtaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubtaskServiceTest {

    @Mock
    private SubtaskRepository subtaskRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private SubtaskService service;

    @Test
    void shouldCreateSubtaskOnlyForOwnedTask() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("ronis", "ronis@example.com", "encoded");
        Task task = new Task();
        task.setUser(user);

        when(authenticatedUserService.getCurrentUser()).thenReturn(withId(user, userId));
        when(taskService.findByIdAndValidateOwnership(taskId, userId)).thenReturn(task);
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subtask result = service.createSubtask(new CreateSubstaskRequestDTO("subtask", taskId));

        ArgumentCaptor<Subtask> captor = ArgumentCaptor.forClass(Subtask.class);
        verify(subtaskRepository).save(captor.capture());
        assertEquals("subtask", captor.getValue().getTitle());
        assertEquals(result.getTitle(), captor.getValue().getTitle());
    }

    @Test
    void shouldRejectUpdatingSubtaskFromAnotherUser() {
        UUID subtaskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("ronis", "ronis@example.com", "encoded");

        when(authenticatedUserService.getCurrentUser()).thenReturn(withId(user, userId));
        when(subtaskRepository.findByIdAndTaskUserId(subtaskId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.completeSubtask(subtaskId, true));
    }

    private User withId(User user, UUID id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
            return user;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
