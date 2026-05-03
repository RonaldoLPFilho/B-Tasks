package com.example.tasksapi.service.howtodo;

import com.example.tasksapi.exception.InvalidDataException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@EnableConfigurationProperties(HowToDoStorageProperties.class)
public class HowToDoStorage {
    private static final String DOCUMENT_FILE_NAME = "document.md";

    private final HowToDoStorageProperties properties;
    private final Path basePath;

    public HowToDoStorage(HowToDoStorageProperties properties) {
        this.properties = properties;
        this.basePath = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
    }

    public StoredMarkdown saveNew(UUID documentId, String content) {
        String storageKey = buildStorageKey(documentId);
        write(storageKey, content);
        return new StoredMarkdown(properties.getProvider(), storageKey);
    }

    public String read(String storageKey) {
        Path path = resolveStorageKey(storageKey);
        if (!Files.isRegularFile(path)) {
            throw new InvalidDataException("Arquivo Markdown não encontrado.");
        }

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new InvalidDataException("Não foi possível ler o Markdown.");
        }
    }

    public void write(String storageKey, String content) {
        String safeContent = content == null ? "" : content;
        validateContentSize(safeContent);
        Path path = resolveStorageKey(storageKey);
        Path temp = path.resolveSibling(DOCUMENT_FILE_NAME + ".tmp");

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temp, safeContent, StandardCharsets.UTF_8);
            Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            deleteQuietly(temp);
            throw new InvalidDataException("Não foi possível salvar o Markdown.");
        }
    }

    public void delete(String storageKey) {
        Path path = resolveStorageKey(storageKey);
        Path directory = path.getParent();

        try {
            if (!Files.exists(directory)) {
                return;
            }

            try (Stream<Path> paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(this::deleteQuietly);
            }
        } catch (IOException exception) {
            throw new InvalidDataException("Não foi possível remover o Markdown.");
        }
    }

    private String buildStorageKey(UUID documentId) {
        return "%s/%s".formatted(documentId, DOCUMENT_FILE_NAME);
    }

    private Path resolveStorageKey(String storageKey) {
        Path resolved = basePath.resolve(storageKey).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new InvalidDataException("Caminho de Markdown inválido.");
        }
        return resolved;
    }

    private void validateContentSize(String content) {
        if (content.getBytes(StandardCharsets.UTF_8).length > properties.getMaxFileSizeBytes()) {
            throw new InvalidDataException("Markdown excede o tamanho máximo permitido.");
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public record StoredMarkdown(String storageProvider, String storageKey) {
    }
}
