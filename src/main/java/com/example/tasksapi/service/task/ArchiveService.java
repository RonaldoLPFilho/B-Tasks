package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Comment;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Subtask;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.dto.ArchivedItemTypeDTO;
import com.example.tasksapi.dto.ArchivedSearchResultDTO;
import com.example.tasksapi.dto.RestoreSectionRequestDTO;
import com.example.tasksapi.dto.RestoreTaskRequestDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.dto.TaskSearchMatchDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TabRepository;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import jakarta.transaction.Transactional;
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
public class ArchiveService {

    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 60;
    private static final int SNIPPET_CONTEXT = 36;

    private final TabRepository tabRepository;
    private final SectionRepository sectionRepository;
    private final TaskRepository taskRepository;
    private final TabService tabService;
    private final SectionService sectionService;
    private final TaskService taskService;
    private final AuthenticatedUserService authenticatedUserService;
    private final TaskResponseMapper taskResponseMapper;

    public ArchiveService(TabRepository tabRepository,
                          SectionRepository sectionRepository,
                          TaskRepository taskRepository,
                          TabService tabService,
                          SectionService sectionService,
                          TaskService taskService,
                          AuthenticatedUserService authenticatedUserService,
                          TaskResponseMapper taskResponseMapper) {
        this.tabRepository = tabRepository;
        this.sectionRepository = sectionRepository;
        this.taskRepository = taskRepository;
        this.tabService = tabService;
        this.sectionService = sectionService;
        this.taskService = taskService;
        this.authenticatedUserService = authenticatedUserService;
        this.taskResponseMapper = taskResponseMapper;
    }

    public List<ArchivedSearchResultDTO> searchArchived(String rawQuery, Integer limit) {
        List<String> terms = normalizeTerms(rawQuery);
        if (terms.isEmpty()) {
            return List.of();
        }

        User user = authenticatedUserService.getCurrentUser();
        List<ArchivedSearchResultDTO> results = new ArrayList<>();

        tabRepository.findByUserIdAndArchivedTrueOrderBySortOrderAsc(user.getId()).stream()
                .map(tab -> toTabResult(tab, terms))
                .filter(result -> !result.matches().isEmpty())
                .forEach(results::add);

        sectionRepository.findArchivedForUser(user.getId()).stream()
                .filter(section -> !section.getTab().isArchived() || section.isArchived())
                .map(section -> toSectionResult(section, terms))
                .filter(result -> !result.matches().isEmpty())
                .forEach(results::add);

        taskRepository.findArchivedForUser(user.getId()).stream()
                .filter(task -> isEffectivelyArchived(task))
                .map(task -> toTaskResult(task, terms))
                .filter(result -> !result.matches().isEmpty())
                .forEach(results::add);

        return results.stream()
                .sorted(Comparator
                        .comparingInt(ArchivedSearchResultDTO::score).reversed()
                        .thenComparing(ArchivedSearchResultDTO::title, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(resolveLimit(limit))
                .toList();
    }

    @Transactional
    public void restoreTab(UUID tabId) {
        tabService.unarchive(tabId);
    }

    @Transactional
    public void restoreSection(UUID sectionId, RestoreSectionRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        Section section = sectionService.findById(sectionId);
        validateSectionOwnership(section, user.getId());

        boolean restoreParents = request != null && Boolean.TRUE.equals(request.restoreParents());
        UUID targetTabId = request != null ? request.targetTabId() : null;

        if (restoreParents) {
            if (section.getTab().isArchived()) {
                tabService.unarchive(section.getTab().getId());
            }
            if (section.isArchived()) {
                section.setArchived(false);
                sectionRepository.save(section);
            }
            return;
        }

        if (targetTabId == null) {
            throw new InvalidDataException("Target tab is required when not restoring parent tab");
        }

        Tab targetTab = tabService.findByIdAndValidateOwnership(targetTabId, user.getId());
        if (targetTab.isArchived()) {
            throw new InvalidDataException("Target tab must be active");
        }

        section.setTab(targetTab);
        section.setArchived(false);
        section.setSortOrder((int) sectionRepository.countActiveByTabId(targetTabId));
        sectionRepository.save(section);
    }

    @Transactional
    public void restoreTask(UUID taskId, RestoreTaskRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        Task task = taskService.findByIdAndValidateOwnership(taskId, user.getId());
        Section currentSection = task.getSection();
        Tab currentTab = currentSection != null ? currentSection.getTab() : null;

        boolean restoreParents = request != null && Boolean.TRUE.equals(request.restoreParents());
        UUID targetTabId = request != null ? request.targetTabId() : null;
        UUID targetSectionId = request != null ? request.targetSectionId() : null;

        if (restoreParents) {
            if (currentTab != null && currentTab.isArchived()) {
                tabService.unarchive(currentTab.getId());
            }
            if (currentSection != null && currentSection.isArchived()) {
                sectionService.unarchive(currentSection.getTab().getId(), currentSection.getId());
            }
            task.setSortOrder(resolveNextSortOrder(currentSection));
            task.setArchived(false);
            taskRepository.save(task);
            return;
        }

        boolean originIsActive = currentTab != null
                && !currentTab.isArchived()
                && currentSection != null
                && !currentSection.isArchived();

        if (originIsActive && targetSectionId == null) {
            task.setSortOrder(resolveNextSortOrder(currentSection));
            task.setArchived(false);
            taskRepository.save(task);
            return;
        }

        if (targetSectionId == null || targetTabId == null) {
            throw new InvalidDataException("Target tab and section are required when origin is unavailable");
        }

        Tab targetTab = tabService.findByIdAndValidateOwnership(targetTabId, user.getId());
        if (targetTab.isArchived()) {
            throw new InvalidDataException("Target tab must be active");
        }

        Section targetSection = sectionService.findByIdAndValidateTab(targetSectionId, targetTabId, user.getId());
        if (targetSection.isArchived()) {
            throw new InvalidDataException("Target section must be active");
        }

        task.setSection(targetSection);
        task.setTab(targetTab);
        task.setSortOrder(resolveNextSortOrder(targetSection));
        task.setArchived(false);
        taskRepository.save(task);
    }

    private ArchivedSearchResultDTO toTabResult(Tab tab, List<String> terms) {
        List<TaskSearchMatchDTO> matches = new ArrayList<>();
        int score = 0;

        score += addMatch(matches, "tabName", "Aba", tab.getName(), terms, 10);

        for (Section section : tab.getSections()) {
            score += addMatch(matches, "sectionName", "Section", section.getName(), terms, 4);
            for (Task task : section.getTasks()) {
                score += addTaskMatches(matches, task, terms, 2, 2, 2);
            }
        }

        return new ArchivedSearchResultDTO(
                ArchivedItemTypeDTO.TAB,
                tab.getId(),
                tab.getName(),
                "Tab arquivada",
                taskResponseMapper.toTabResponse(tab, true),
                null,
                null,
                tab.getId(),
                tab.getName(),
                true,
                null,
                null,
                false,
                score,
                deduplicateMatches(matches)
        );
    }

    private ArchivedSearchResultDTO toSectionResult(Section section, List<String> terms) {
        List<TaskSearchMatchDTO> matches = new ArrayList<>();
        int score = 0;

        score += addMatch(matches, "sectionName", "Section", section.getName(), terms, 9);
        score += addMatch(matches, "tabName", "Aba", section.getTab().getName(), terms, 5);

        for (Task task : section.getTasks()) {
            score += addTaskMatches(matches, task, terms, 3, 3, 3);
        }

        return new ArchivedSearchResultDTO(
                ArchivedItemTypeDTO.SECTION,
                section.getId(),
                section.getName(),
                "Section arquivada",
                null,
                taskResponseMapper.toSectionResponse(section, true),
                null,
                section.getTab().getId(),
                section.getTab().getName(),
                section.getTab().isArchived(),
                section.getId(),
                section.getName(),
                section.isArchived(),
                score,
                deduplicateMatches(matches)
        );
    }

    private ArchivedSearchResultDTO toTaskResult(Task task, List<String> terms) {
        List<TaskSearchMatchDTO> matches = new ArrayList<>();
        int score = addTaskMatches(matches, task, terms, 10, 6, 8);
        score += addMatch(matches, "tabName", "Aba", task.getSection() != null && task.getSection().getTab() != null
                ? task.getSection().getTab().getName()
                : null, terms, 7);
        score += addMatch(matches, "sectionName", "Section", task.getSection() != null
                ? task.getSection().getName()
                : null, terms, 5);

        return new ArchivedSearchResultDTO(
                ArchivedItemTypeDTO.TASK,
                task.getId(),
                task.getTitle(),
                "Task arquivada",
                null,
                null,
                taskResponseMapper.toTaskResponse(task),
                task.getSection() != null && task.getSection().getTab() != null ? task.getSection().getTab().getId() : null,
                task.getSection() != null && task.getSection().getTab() != null ? task.getSection().getTab().getName() : null,
                task.getSection() != null && task.getSection().getTab() != null && task.getSection().getTab().isArchived(),
                task.getSection() != null ? task.getSection().getId() : null,
                task.getSection() != null ? task.getSection().getName() : null,
                task.getSection() != null && task.getSection().isArchived(),
                score,
                deduplicateMatches(matches)
        );
    }

    private int addTaskMatches(List<TaskSearchMatchDTO> matches,
                               Task task,
                               List<String> terms,
                               int titleWeight,
                               int descriptionWeight,
                               int idWeight) {
        int score = 0;
        score += addMatch(matches, "title", "Titulo", task.getTitle(), terms, titleWeight);
        score += addMatch(matches, "description", "Descricao", task.getDescription(), terms, descriptionWeight);
        score += addMatch(matches, "jiraId", "Jira ID", task.getJiraId(), terms, 6);
        score += addMatch(matches, "taskId", "ID da task", task.getId().toString(), terms, idWeight);

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

        return score;
    }

    private List<TaskSearchMatchDTO> deduplicateMatches(List<TaskSearchMatchDTO> matches) {
        return matches.stream()
                .collect(Collectors.toMap(
                        match -> match.field() + ":" + match.snippet(),
                        match -> match,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
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

    private List<String> normalizeTerms(String rawQuery) {
        if (rawQuery == null) {
            return List.of();
        }

        Set<String> terms = java.util.Arrays.stream(rawQuery.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return List.copyOf(terms);
    }

    private boolean isEffectivelyArchived(Task task) {
        return task.isArchived()
                || (task.getSection() != null && task.getSection().isArchived())
                || (task.getSection() != null && task.getSection().getTab() != null && task.getSection().getTab().isArchived());
    }

    private void validateSectionOwnership(Section section, UUID userId) {
        if (!section.getTab().getUser().getId().equals(userId)) {
            throw new NotFoundException("Section not found with id " + section.getId());
        }
    }

    private int resolveNextSortOrder(Section section) {
        if (section == null || section.getId() == null) {
            return 0;
        }

        return taskRepository.findMaxSortOrderBySectionId(section.getId()) + 1;
    }
}
