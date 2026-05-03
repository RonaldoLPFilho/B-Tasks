package com.example.tasksapi.domain.vault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vault_entry", indexes = {
        @Index(name = "idx_vault_entry_item", columnList = "item_id")
})
public class VaultEntry {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "encrypted_key", nullable = false, columnDefinition = "text")
    private String encryptedKey;

    @Column(name = "encrypted_value", nullable = false, columnDefinition = "text")
    private String encryptedValue;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private VaultItem item;

    public UUID getId() {
        return id;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public VaultItem getItem() {
        return item;
    }

    public void setItem(VaultItem item) {
        this.item = item;
    }
}
