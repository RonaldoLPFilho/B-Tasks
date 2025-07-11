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

}
