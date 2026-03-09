package com.example.tasksapi.domain.task;

import com.example.tasksapi.domain.Auditable;
import com.example.tasksapi.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "tab",
        indexes = {
                @Index(name = "idx_tab_user_id", columnList = "user_id")
        }
)
public class Tab extends Auditable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 20)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "tab", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Section> sections = new ArrayList<>();

    public Tab() {
    }

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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public boolean hasTasks() {
        return sections.stream().anyMatch(s -> !s.getTasks().isEmpty());
    }
}
