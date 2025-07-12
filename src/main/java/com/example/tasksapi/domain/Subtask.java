package com.example.tasksapi.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Subtask {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String title;

    private boolean completed;

    @ManyToOne
    private Task task;

    public Subtask(String title, boolean completed, Task task) {
        this.title = title;
        this.completed = completed;
        this.task = task;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
