package com.example.tasksapi.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Comment {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String description;

    private LocalDateTime createdAt;

    private String author;

    @ManyToOne
    @JoinColumn(name = "task_id")
    @JsonBackReference
    private Task task;


    public Comment(String description, String author, Task task) {
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.author = author;
        this.task = task;
    }

    public Comment() {}

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
