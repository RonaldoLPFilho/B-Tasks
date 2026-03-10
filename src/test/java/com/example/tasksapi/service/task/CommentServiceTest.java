package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Comment;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.CreateCommentRequestDTO;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.CommentRepository;
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
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private CommentService service;

    @Test
    void shouldCreateCommentUsingCurrentUserAndOwnedTask() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("ronis", "ronis@example.com", "encoded");
        user.setPassword("encoded");
        Task task = new Task();
        task.setUser(user);

        when(authenticatedUserService.getCurrentUser()).thenReturn(withId(user, userId));
        when(taskService.findByIdAndValidateOwnership(taskId, userId)).thenReturn(task);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = service.createComment(new CreateCommentRequestDTO("novo comentario", taskId));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertEquals("novo comentario", captor.getValue().getDescription());
        assertEquals("ronis", captor.getValue().getAuthor());
        assertEquals(result.getAuthor(), captor.getValue().getAuthor());
    }

    @Test
    void shouldRejectDeletingCommentFromAnotherUser() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("ronis", "ronis@example.com", "encoded");

        when(authenticatedUserService.getCurrentUser()).thenReturn(withId(user, userId));
        when(commentRepository.findByIdAndTaskUserId(commentId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteById(commentId));
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
