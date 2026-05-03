package com.example.tasksapi.domain.howtodo;

import com.example.tasksapi.domain.Auditable;
import com.example.tasksapi.domain.User;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "how_to_do_document",
        indexes = {
                @Index(name = "idx_how_to_do_user_deleted_updated", columnList = "user_id, deleted, updated_at"),
                @Index(name = "idx_how_to_do_user_title", columnList = "user_id, title")
        }
)
public class HowToDoDocument extends Auditable {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "storage_provider", nullable = false)
    private String storageProvider;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false)
    private boolean deleted = false;

    public HowToDoDocument() {
    }

    public HowToDoDocument(UUID id, User user, String title, String storageProvider, String storageKey) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
