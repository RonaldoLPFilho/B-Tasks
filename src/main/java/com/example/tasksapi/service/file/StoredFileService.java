package com.example.tasksapi.service.file;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.file.StoredFile;
import com.example.tasksapi.dto.StoredFileDTO;
import com.example.tasksapi.dto.StoredFilesPageDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.StoredFileRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StoredFileService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final StoredFileRepository storedFileRepository;
    private final FileStorage fileStorage;
    private final AuthenticatedUserService authenticatedUserService;

    public StoredFileService(StoredFileRepository storedFileRepository,
                             FileStorage fileStorage,
                             AuthenticatedUserService authenticatedUserService) {
        this.storedFileRepository = storedFileRepository;
        this.fileStorage = fileStorage;
        this.authenticatedUserService = authenticatedUserService;
    }

    public StoredFileDTO upload(MultipartFile multipartFile) {
        User user = authenticatedUserService.getCurrentUser();
        UUID fileId = UUID.randomUUID();
        String originalFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename() == null ? "" : multipartFile.getOriginalFilename());
        StoredObject storedObject = fileStorage.save(user.getId(), fileId, multipartFile);

        StoredFile storedFile = new StoredFile(
                fileId,
                user,
                originalFileName,
                storedObject.storedFileName(),
                storedObject.contentType(),
                storedObject.sizeBytes(),
                storedObject.checksumSha256(),
                storedObject.storageProvider(),
                storedObject.storageKey(),
                LocalDateTime.now()
        );

        try {
            return toDto(storedFileRepository.save(storedFile));
        } catch (RuntimeException exception) {
            fileStorage.delete(storedObject.storageKey());
            throw exception;
        }
    }

    public StoredFilesPageDTO search(String name, LocalDate uploadedFrom, LocalDate uploadedTo, Integer page, Integer size) {
        User user = authenticatedUserService.getCurrentUser();
        int safePage = Math.max(page == null ? 0 : page, 0);
        int safeSize = Math.min(Math.max(size == null ? DEFAULT_PAGE_SIZE : size, 1), MAX_PAGE_SIZE);
        String trimmedName = StringUtils.hasText(name) ? name.trim() : null;
        LocalDateTime from = uploadedFrom == null ? null : uploadedFrom.atStartOfDay();
        LocalDateTime to = uploadedTo == null ? null : uploadedTo.atTime(LocalTime.MAX);

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidDataException("Data inicial não pode ser maior que a data final.");
        }

        Page<StoredFile> result = storedFileRepository.findAll(
                fileFilters(user.getId(), trimmedName, from, to),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "uploadedAt"))
        );

        return new StoredFilesPageDTO(
                result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    public FileDownload loadForDownload(UUID fileId) {
        User user = authenticatedUserService.getCurrentUser();
        StoredFile storedFile = findForCurrentUser(fileId, user.getId());
        Resource resource = fileStorage.load(storedFile.getStorageKey());
        return new FileDownload(storedFile, resource);
    }

    public void delete(UUID fileId) {
        User user = authenticatedUserService.getCurrentUser();
        StoredFile storedFile = findForCurrentUser(fileId, user.getId());
        fileStorage.delete(storedFile.getStorageKey());
        storedFileRepository.delete(storedFile);
    }

    private StoredFile findForCurrentUser(UUID fileId, UUID userId) {
        return storedFileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new NotFoundException("Arquivo não encontrado."));
    }

    private StoredFileDTO toDto(StoredFile storedFile) {
        return new StoredFileDTO(
                storedFile.getId(),
                storedFile.getOriginalFileName(),
                storedFile.getContentType(),
                storedFile.getSizeBytes(),
                storedFile.getChecksumSha256(),
                storedFile.getUploadedAt()
        );
    }

    private Specification<StoredFile> fileFilters(UUID userId, String name, LocalDateTime uploadedFrom, LocalDateTime uploadedTo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("originalFileName")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (uploadedFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("uploadedAt"), uploadedFrom));
            }

            if (uploadedTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("uploadedAt"), uploadedTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public record FileDownload(StoredFile metadata, Resource resource) {
    }
}
