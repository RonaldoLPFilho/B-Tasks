package com.example.tasksapi.domain;

import com.example.tasksapi.domain.task.Task;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tab",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_tab_user_name", columnNames = {"user_id", "name"})
    },
    indexes = {
        @Index(name = "idx_tab_user", columnList = "user_id")
    }
)
public class Tab extends Auditable{
    @Id @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tab_user"))
    private User user;

    @OneToMany(mappedBy = "tab", fetch = FetchType.LAZY)
    private List<Task> tasks;

    public Tab() {}

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
