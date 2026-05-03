package com.example.tasksapi.service.file;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.file.StoredFile;
import com.example.tasksapi.dto.StoredFilesPageDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.repository.StoredFileRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoredFileServiceTest {
    @Mock
    private StoredFileRepository storedFileRepository;
    @Mock
    private FileStorage fileStorage;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private StoredFileService storedFileService;

    @Test
    void shouldPersistOnlyMetadataAfterStorageSave() {
        User user = userWithId();
        MockMultipartFile multipartFile = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(fileStorage.save(eq(user.getId()), any(UUID.class), eq(multipartFile))).thenReturn(new StoredObject(
                "original",
                "application/pdf",
                7,
                "checksum",
                "local",
                "users/%s/files/aa/bb/file/original".formatted(user.getId())
        ));
        when(storedFileRepository.save(any(StoredFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = storedFileService.upload(multipartFile);

        ArgumentCaptor<StoredFile> captor = ArgumentCaptor.forClass(StoredFile.class);
        verify(storedFileRepository).save(captor.capture());
        StoredFile saved = captor.getValue();
        assertEquals(result.id(), saved.getId());
        assertEquals("report.pdf", saved.getOriginalFileName());
        assertEquals("local", saved.getStorageProvider());
        assertEquals(user, saved.getUser());
    }

    @Test
    void shouldCleanupPhysicalFileWhenMetadataPersistenceFails() {
        User user = userWithId();
        MockMultipartFile multipartFile = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
        StoredObject storedObject = new StoredObject("original", "application/pdf", 7, "checksum", "local", "key");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(fileStorage.save(eq(user.getId()), any(UUID.class), eq(multipartFile))).thenReturn(storedObject);
        when(storedFileRepository.save(any(StoredFile.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class, () -> storedFileService.upload(multipartFile));

        verify(fileStorage).delete("key");
    }

    @Test
    void shouldSearchCurrentUserFilesWithInclusiveDateRange() {
        User user = userWithId();
        StoredFile storedFile = storedFile(user, "notes.txt");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(storedFileRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(storedFile)));

        StoredFilesPageDTO page = storedFileService.search(" note ", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3), 0, 10);

        assertEquals(1, page.totalElements());
        assertEquals("notes.txt", page.content().get(0).originalFileName());
        verify(storedFileRepository).findAll(any(Specification.class), argThat((Pageable pageable) ->
                pageable.getPageNumber() == 0
                        && pageable.getPageSize() == 10
                        && pageable.getSort().getOrderFor("uploadedAt") != null
        ));
    }

    @Test
    void shouldRejectInvertedDateRange() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(userWithId());

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> storedFileService.search(null, LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 1), 0, 10)
        );

        assertEquals("Data inicial não pode ser maior que a data final.", exception.getMessage());
        verify(storedFileRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldDeleteOnlyCurrentUserFile() {
        User user = userWithId();
        StoredFile storedFile = storedFile(user, "notes.txt");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(storedFileRepository.findByIdAndUserId(storedFile.getId(), user.getId())).thenReturn(Optional.of(storedFile));

        storedFileService.delete(storedFile.getId());

        verify(fileStorage).delete(storedFile.getStorageKey());
        verify(storedFileRepository).delete(storedFile);
    }

    private User userWithId() {
        User user = new User();
        setField(user, "id", UUID.randomUUID());
        user.setUsername("ronaldo");
        user.setEmail("ronaldo@example.com");
        return user;
    }

    private StoredFile storedFile(User user, String name) {
        return new StoredFile(
                UUID.randomUUID(),
                user,
                name,
                "original",
                "text/plain",
                10,
                "checksum",
                "local",
                "users/%s/files/aa/bb/file/original".formatted(user.getId()),
                LocalDateTime.now()
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
