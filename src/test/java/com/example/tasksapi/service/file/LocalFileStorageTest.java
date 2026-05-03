package com.example.tasksapi.service.file;

import com.example.tasksapi.exception.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldStoreFileUsingUserAndUuidShardedPath() throws Exception {
        LocalFileStorageProperties properties = new LocalFileStorageProperties();
        properties.setBasePath(tempDir.toString());
        LocalFileStorage storage = new LocalFileStorage(properties);
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "content".getBytes()
        );

        StoredObject stored = storage.save(userId, fileId, file);

        assertEquals("local", stored.storageProvider());
        assertEquals("users/%s/files/12/34/%s/original".formatted(userId, fileId), stored.storageKey());
        assertEquals(7, stored.sizeBytes());
        assertTrue(Files.isRegularFile(tempDir.resolve(stored.storageKey())));
        assertEquals("ed7002b439e9ac845f22357d822bac1444730fbdb6016d3ec9432297b9ec9f73", stored.checksumSha256());
    }

    @Test
    void shouldRejectFilesLargerThanConfiguredLimit() {
        LocalFileStorageProperties properties = new LocalFileStorageProperties();
        properties.setBasePath(tempDir.toString());
        properties.setMaxFileSizeBytes(3);
        LocalFileStorage storage = new LocalFileStorage(properties);
        MockMultipartFile file = new MockMultipartFile("file", "big.txt", "text/plain", "large".getBytes());

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> storage.save(UUID.randomUUID(), UUID.randomUUID(), file)
        );

        assertEquals("Arquivo excede o tamanho máximo permitido.", exception.getMessage());
    }

    @Test
    void shouldDeleteStoredFile() {
        LocalFileStorageProperties properties = new LocalFileStorageProperties();
        properties.setBasePath(tempDir.toString());
        LocalFileStorage storage = new LocalFileStorage(properties);
        StoredObject stored = storage.save(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MockMultipartFile("file", "note.txt", "text/plain", "abc".getBytes())
        );

        storage.delete(stored.storageKey());

        assertFalse(Files.exists(tempDir.resolve(stored.storageKey())));
    }
}
