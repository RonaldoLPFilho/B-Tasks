package com.example.tasksapi.domain.task;

import com.example.tasksapi.domain.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "section",
        indexes = {
                @Index(name = "idx_section_tab_id", columnList = "tab_id")
        }
)
public class Section extends Auditable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "tab_id", nullable = false)
    @JsonIgnore
    private Tab tab;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column
    private Boolean archived = false;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Task> tasks = new ArrayList<>();

    public Section() {
    }

    public Section(String name, Tab tab, int sortOrder) {
        this.name = name;
        this.tab = tab;
        this.sortOrder = sortOrder;
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

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public boolean isArchived() {
        return Boolean.TRUE.equals(archived);
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
