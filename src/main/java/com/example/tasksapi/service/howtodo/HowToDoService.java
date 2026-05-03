package com.example.tasksapi.service.howtodo;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.howtodo.HowToDoDocument;
import com.example.tasksapi.dto.CreateHowToDoRequestDTO;
import com.example.tasksapi.dto.HowToDoDetailDTO;
import com.example.tasksapi.dto.HowToDoPageDTO;
import com.example.tasksapi.dto.HowToDoSummaryDTO;
import com.example.tasksapi.dto.UpdateHowToDoRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.HowToDoDocumentRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HowToDoService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final HowToDoDocumentRepository repository;
    private final HowToDoStorage storage;
    private final AuthenticatedUserService authenticatedUserService;

    public HowToDoService(HowToDoDocumentRepository repository,
                          HowToDoStorage storage,
                          AuthenticatedUserService authenticatedUserService) {
        this.repository = repository;
        this.storage = storage;
        this.authenticatedUserService = authenticatedUserService;
    }

    public HowToDoPageDTO list(String title, Integer page, Integer size) {
        User user = authenticatedUserService.getCurrentUser();
        int safePage = Math.max(page == null ? 0 : page, 0);
        int safeSize = Math.min(Math.max(size == null ? DEFAULT_PAGE_SIZE : size, 1), MAX_PAGE_SIZE);

        Page<HowToDoDocument> result = repository.findAll(
                filters(user.getId(), title),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );

        return new HowToDoPageDTO(
                result.getContent().stream().map(this::toSummary).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional
    public HowToDoDetailDTO create(CreateHowToDoRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        String title = validateTitle(request == null ? null : request.title());
        String content = request == null ? "" : request.content();
        UUID id = UUID.randomUUID();

        HowToDoStorage.StoredMarkdown storedMarkdown = storage.saveNew(id, content);
        HowToDoDocument document = new HowToDoDocument(
                id,
                user,
                title,
                storedMarkdown.storageProvider(),
                storedMarkdown.storageKey()
        );

        try {
            return toDetail(repository.save(document), content);
        } catch (RuntimeException exception) {
            storage.delete(storedMarkdown.storageKey());
            throw exception;
        }
    }

    public HowToDoDetailDTO get(UUID id) {
        User user = authenticatedUserService.getCurrentUser();
        HowToDoDocument document = findForCurrentUser(id, user.getId());
        return toDetail(document, storage.read(document.getStorageKey()));
    }

    @Transactional
    public HowToDoDetailDTO update(UUID id, UpdateHowToDoRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        HowToDoDocument document = findForCurrentUser(id, user.getId());
        String title = validateTitle(request == null ? null : request.title());
        String content = request == null ? "" : request.content();

        storage.write(document.getStorageKey(), content);
        document.setTitle(title);

        return toDetail(repository.save(document), content);
    }

    @Transactional
    public void delete(UUID id) {
        User user = authenticatedUserService.getCurrentUser();
        HowToDoDocument document = findForCurrentUser(id, user.getId());

        storage.delete(document.getStorageKey());
        document.setDeleted(true);
        repository.save(document);
    }

    private HowToDoDocument findForCurrentUser(UUID id, UUID userId) {
        return repository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("How To Do não encontrado."));
    }

    private String validateTitle(String rawTitle) {
        if (!StringUtils.hasText(rawTitle)) {
            throw new InvalidDataException("Título é obrigatório.");
        }

        String title = rawTitle.trim();
        if (title.length() > 160) {
            throw new InvalidDataException("Título deve ter no máximo 160 caracteres.");
        }
        return title;
    }

    private Specification<HowToDoDocument> filters(UUID userId, String title) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (StringUtils.hasText(title)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private HowToDoSummaryDTO toSummary(HowToDoDocument document) {
        return new HowToDoSummaryDTO(
                document.getId(),
                document.getTitle(),
                safeDate(document.getCreatedAt()),
                safeDate(document.getUpdatedAt())
        );
    }

    private HowToDoDetailDTO toDetail(HowToDoDocument document, String content) {
        return new HowToDoDetailDTO(
                document.getId(),
                document.getTitle(),
                content,
                safeDate(document.getCreatedAt()),
                safeDate(document.getUpdatedAt())
        );
    }

    private LocalDateTime safeDate(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }
}
