package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Comment;
import com.example.tasksapi.domain.task.Subtask;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.dto.TaskSearchMatchDTO;
import com.example.tasksapi.dto.TaskSearchResultDTO;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskSearchService {

    private static final int DEFAULT_LIMIT = 25;
    private static final int MAX_LIMIT = 50;
    private static final int SNIPPET_CONTEXT = 36;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TaskService taskService;
    private final TaskResponseMapper taskResponseMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public TaskSearchService(NamedParameterJdbcTemplate jdbcTemplate,
                             TaskService taskService,
                             TaskResponseMapper taskResponseMapper,
                             AuthenticatedUserService authenticatedUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskService = taskService;
        this.taskResponseMapper = taskResponseMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<TaskSearchResultDTO> search(String rawQuery, UUID tabId, Integer limit, ArchiveScope scope) {
        List<String> terms = normalizeTerms(rawQuery);
        if (terms.isEmpty()) {
            return List.of();
        }

        User user = authenticatedUserService.getCurrentUser();
        List<UUID> taskIds = searchCandidateTaskIds(user.getId(), tabId, terms, limit, scope);
        if (taskIds.isEmpty()) {
            return List.of();
        }

        return taskIds.stream()
                .map(taskService::findByIdForCurrentUser)
                .map(task -> toSearchResult(task, terms))
                .filter(result -> !result.matches().isEmpty())
                .sorted(Comparator
                        .comparingInt(TaskSearchResultDTO::score).reversed()
                        .thenComparing(result -> result.task().createdAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(resolveLimit(limit))
                .toList();
    }

    List<UUID> searchCandidateTaskIds(UUID userId, UUID tabId, List<String> terms, Integer limit, ArchiveScope scope) {
        StringBuilder sql = new StringBuilder("""
                SELECT t.id
                FROM task t
                LEFT JOIN section s ON s.id = t.section_id
                LEFT JOIN tab tb ON tb.id = COALESCE(s.tab_id, t.tab_id)
                WHERE t.user_id = :userId
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("tabId", tabId);

        if (tabId != null) {
            sql.append(" AND s.tab_id = :tabId");
        }

        switch (scope) {
            case ACTIVE -> sql.append("""
                     AND t.active = true
                     AND COALESCE(s.archived, false) = false
                     AND COALESCE(tb.archived, false) = false
                    """);
            case ARCHIVED -> sql.append("""
                     AND (
                        t.active = false
                        OR COALESCE(s.archived, false) = true
                        OR COALESCE(tb.archived, false) = true
                     )
                    """);
            case ALL -> {
            }
        }

        for (int i = 0; i < terms.size(); i++) {
            String paramName = "term" + i;
            sql.append("""
                     AND (
                        LOWER(COALESCE(t.title, '')) LIKE :%s
                        OR LOWER(COALESCE(t.description, '')) LIKE :%s
                        OR LOWER(COALESCE(t.jira_id, '')) LIKE :%s
                        OR LOWER(COALESCE(tb.name, '')) LIKE :%s
                        OR LOWER(COALESCE(s.name, '')) LIKE :%s
                        OR CAST(t.id AS TEXT) LIKE :%s
                        OR EXISTS (
                            SELECT 1
                            FROM "comment" c
                            WHERE c.task_id = t.id
                              AND LOWER(COALESCE(c.description, '')) LIKE :%s
                        )
                        OR EXISTS (
                            SELECT 1
                            FROM subtask st
                            WHERE st.task_id = t.id
                              AND LOWER(COALESCE(st.title, '')) LIKE :%s
                        )
                     )
                    """.formatted(paramName, paramName, paramName, paramName, paramName, paramName, paramName, paramName));
            params.addValue(paramName, "%" + terms.get(i) + "%");
        }

        sql.append(" ORDER BY t.created_at DESC NULLS LAST, t.id");
        sql.append(" LIMIT :limit");
        params.addValue("limit", resolveLimit(limit));

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
    }

    private TaskSearchResultDTO toSearchResult(Task task, List<String> terms) {
        List<TaskSearchMatchDTO> matches = new ArrayList<>();
        int score = 0;

        score += addMatch(matches, "title", "Titulo", task.getTitle(), terms, 10);
        score += addMatch(matches, "description", "Descricao", task.getDescription(), terms, 6);
        score += addMatch(matches, "jiraId", "Jira ID", task.getJiraId(), terms, 8);
        score += addMatch(matches, "tabName", "Aba",
                task.getSection() != null && task.getSection().getTab() != null
                        ? task.getSection().getTab().getName()
                        : null,
                terms, 7);
        score += addMatch(matches, "sectionName", "Section",
                task.getSection() != null ? task.getSection().getName() : null,
                terms, 5);
        score += addMatch(matches, "taskId", "ID da task", task.getId().toString(), terms, 8);

        List<Comment> comments = task.getComments() != null ? task.getComments() : List.of();
        Comment matchingComment = comments.stream()
                .filter(comment -> containsAny(comment.getDescription(), terms))
                .findFirst()
                .orElse(null);
        if (matchingComment != null) {
            score += addMatch(matches, "comments", "Comentario", matchingComment.getDescription(), terms, 4);
        }

        List<Subtask> subtasks = task.getSubtasks() != null ? task.getSubtasks() : List.of();
        Subtask matchingSubtask = subtasks.stream()
                .filter(subtask -> containsAny(subtask.getTitle(), terms))
                .findFirst()
                .orElse(null);
        if (matchingSubtask != null) {
            score += addMatch(matches, "subtasks", "Subtarefa", matchingSubtask.getTitle(), terms, 5);
        }

        return new TaskSearchResultDTO(
                taskResponseMapper.toTaskResponse(task),
                task.getSection() != null && task.getSection().getTab() != null ? task.getSection().getTab().getName() : null,
                task.getSection() != null && task.getSection().getTab() != null && task.getSection().getTab().isArchived(),
                task.getSection() != null ? task.getSection().getName() : null,
                task.getSection() != null && task.getSection().isArchived(),
                score,
                matches
        );
    }

    private int addMatch(List<TaskSearchMatchDTO> matches,
                         String field,
                         String label,
                         String source,
                         List<String> terms,
                         int weight) {
        if (source == null || source.isBlank()) {
            return 0;
        }

        List<String> matchedTerms = terms.stream()
                .filter(term -> contains(source, term))
                .distinct()
                .toList();

        if (matchedTerms.isEmpty()) {
            return 0;
        }

        matches.add(new TaskSearchMatchDTO(
                field,
                label,
                createSnippet(source, matchedTerms),
                matchedTerms
        ));

        return weight * matchedTerms.size();
    }

    private boolean containsAny(String value, List<String> terms) {
        return value != null && terms.stream().anyMatch(term -> contains(value, term));
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private String createSnippet(String source, List<String> matchedTerms) {
        String normalizedSource = source.trim();
        String lowerSource = normalizedSource.toLowerCase(Locale.ROOT);
        int matchIndex = matchedTerms.stream()
                .mapToInt(lowerSource::indexOf)
                .filter(index -> index >= 0)
                .min()
                .orElse(0);

        int start = Math.max(0, matchIndex - SNIPPET_CONTEXT);
        String firstMatchedTerm = matchedTerms.get(0);
        int end = Math.min(normalizedSource.length(), matchIndex + firstMatchedTerm.length() + SNIPPET_CONTEXT);

        String prefix = start > 0 ? "..." : "";
        String suffix = end < normalizedSource.length() ? "..." : "";
        return prefix + normalizedSource.substring(start, end) + suffix;
    }

    private int resolveLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(requestedLimit, MAX_LIMIT);
    }

    List<String> normalizeTerms(String rawQuery) {
        if (rawQuery == null) {
            return List.of();
        }

        Set<String> terms = java.util.Arrays.stream(rawQuery.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return List.copyOf(terms);
    }
}
