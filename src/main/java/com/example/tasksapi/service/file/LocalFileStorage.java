package com.example.tasksapi.service.file;

import com.example.tasksapi.exception.InvalidDataException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Component
@EnableConfigurationProperties(LocalFileStorageProperties.class)
public class LocalFileStorage implements FileStorage {
    private static final String STORED_FILE_NAME = "original";

    private final LocalFileStorageProperties properties;
    private final Path basePath;

    public LocalFileStorage(LocalFileStorageProperties properties) {
        this.properties = properties;
        this.basePath = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject save(UUID userId, UUID fileId, MultipartFile file) {
        validateFile(file);

        String storageKey = buildStorageKey(userId, fileId);
        Path target = resolveStorageKey(storageKey);
        Path temp = target.resolveSibling(STORED_FILE_NAME + ".tmp");

        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            return new StoredObject(
                    STORED_FILE_NAME,
                    file.getContentType(),
                    file.getSize(),
                    HexFormat.of().formatHex(digest.digest()),
                    properties.getProvider(),
                    storageKey
            );
        } catch (IOException | NoSuchAlgorithmException exception) {
            deleteQuietly(temp);
            deleteQuietly(target);
            throw new InvalidDataException("Não foi possível armazenar o arquivo.");
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path path = resolveStorageKey(storageKey);
        if (!Files.isRegularFile(path)) {
            throw new InvalidDataException("Arquivo físico não encontrado.");
        }
        return new FileSystemResource(path);
    }

    @Override
    public void delete(String storageKey) {
        Path path = resolveStorageKey(storageKey);
        try {
            Files.deleteIfExists(path);
            deleteEmptyParents(path.getParent(), basePath);
        } catch (IOException exception) {
            throw new InvalidDataException("Não foi possível remover o arquivo físico.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDataException("Arquivo vazio não pode ser enviado.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!StringUtils.hasText(originalFileName) || originalFileName.contains("..")) {
            throw new InvalidDataException("Nome de arquivo inválido.");
        }

        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new InvalidDataException("Arquivo excede o tamanho máximo permitido.");
        }
    }

    private String buildStorageKey(UUID userId, UUID fileId) {
        String id = fileId.toString().replace("-", "");
        return "users/%s/files/%s/%s/%s/%s".formatted(userId, id.substring(0, 2), id.substring(2, 4), fileId, STORED_FILE_NAME);
    }

    private Path resolveStorageKey(String storageKey) {
        Path resolved = basePath.resolve(storageKey).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new InvalidDataException("Caminho de arquivo inválido.");
        }
        return resolved;
    }

    private void deleteEmptyParents(Path current, Path stopAt) throws IOException {
        while (current != null && !current.equals(stopAt) && Files.isDirectory(current)) {
            try (var entries = Files.list(current)) {
                if (entries.findAny().isPresent()) {
                    return;
                }
            }
            Files.deleteIfExists(current);
            current = current.getParent();
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
