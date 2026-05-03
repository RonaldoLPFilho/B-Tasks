package com.example.tasksapi.domain.vault;

import com.example.tasksapi.domain.Auditable;
import com.example.tasksapi.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vault_item", indexes = {
        @Index(name = "idx_vault_item_user", columnList = "user_id")
})
public class VaultItem extends Auditable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "encrypted_name", nullable = false, columnDefinition = "text")
    private String encryptedName;

    @Column(name = "encrypted_description", columnDefinition = "text")
    private String encryptedDescription;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<VaultEntry> entries = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public String getEncryptedName() {
        return encryptedName;
    }

    public void setEncryptedName(String encryptedName) {
        this.encryptedName = encryptedName;
    }

    public String getEncryptedDescription() {
        return encryptedDescription;
    }

    public void setEncryptedDescription(String encryptedDescription) {
        this.encryptedDescription = encryptedDescription;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<VaultEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<VaultEntry> entries) {
        this.entries = entries;
    }
}
