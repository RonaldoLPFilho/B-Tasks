package com.example.tasksapi.domain.file;

import com.example.tasksapi.domain.Auditable;
import com.example.tasksapi.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stored_file",
        indexes = {
                @Index(name = "idx_stored_file_user_uploaded", columnList = "user_id, uploaded_at"),
                @Index(name = "idx_stored_file_user_name", columnList = "user_id, original_file_name")
        }
)
public class StoredFile extends Auditable {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "storage_provider", nullable = false)
    private String storageProvider;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    public StoredFile() {
    }

    public StoredFile(UUID id,
                      User user,
                      String originalFileName,
                      String storedFileName,
                      String contentType,
                      long sizeBytes,
                      String checksumSha256,
                      String storageProvider,
                      String storageKey,
                      LocalDateTime uploadedAt) {
        this.id = id;
        this.user = user;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksumSha256 = checksumSha256;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
