package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Comment;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Subtask;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.dto.TaskSearchResultDTO;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskSearchServiceTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock
    private TaskService taskService;
    @Spy
    private TaskResponseMapper taskResponseMapper;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private TaskSearchService taskSearchService;

    @Test
    void shouldNormalizeTermsRemovingDuplicatesAndWhitespace() {
        List<String> terms = taskSearchService.normalizeTerms("  Login   jwt login  ");

        assertEquals(List.of("login", "jwt"), terms);
    }

    @Test
    void shouldBuildSearchResultWithHighlightsAcrossTaskFields() {
        Task task = createTask();

        TaskSearchResultDTO result = invokeToSearchResult(task, List.of("login"));

        assertEquals("Backend", result.tabName());
        assertEquals("Geral", result.sectionName());
        assertFalse(result.matches().isEmpty());
        assertTrue(result.matches().stream().anyMatch(match -> "title".equals(match.field())));
        assertTrue(result.matches().stream().anyMatch(match -> "comments".equals(match.field())));
    }

    private TaskSearchResultDTO invokeToSearchResult(Task task, List<String> terms) {
        try {
            var method = TaskSearchService.class.getDeclaredMethod("toSearchResult", Task.class, List.class);
            method.setAccessible(true);
            return (TaskSearchResultDTO) method.invoke(taskSearchService, task, terms);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Task createTask() {
        User user = new User("ronis", "ronis@example.com", "encoded");
        setField(user, "id", UUID.randomUUID());

        Tab tab = new Tab();
        tab.setName("Backend");

        Section section = new Section();
        section.setName("Geral");
        section.setTab(tab);

        Task task = new Task("Corrigir login JWT", "Fluxo de autenticacao falha", user, section, "AUTH-77", null);
        setField(task, "id", UUID.randomUUID());
        setField(task, "createdAt", LocalDateTime.now());
        setField(task, "comments", new java.util.ArrayList<>(List.of(
                new Comment("Erro aparece depois do login", "ronis", task)
        )));
        setField(task, "subtasks", new java.util.ArrayList<>(List.of(
                new Subtask("Validar token de login", task)
        )));
        return task;
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
