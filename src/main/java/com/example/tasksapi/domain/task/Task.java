package com.example.tasksapi.domain.task;

import com.example.tasksapi.domain.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "task",
        indexes = {
                @Index(name = "idx_task_user", columnList = "user_id"),
                @Index(name = "idx_task_section", columnList = "section_id"),
                @Index(name = "idx_task_tab", columnList = "tab_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_task_section_sort", columnNames = {"section_id", "sort_order"})
        }
)
public class Task extends Auditable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private boolean completed;

    private LocalDate finishedAt;

    @Column(nullable = true)
    private String jiraId;

    @ManyToOne
    @JoinColumn(name = "section_id")
    @JsonBackReference
    private Section section;

    @ManyToOne
    @JoinColumn(name = "tab_id")
    @JsonIgnore
    private Tab tab;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Subtask> subtasks;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Comment> comments;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "active", nullable = false)
    private boolean active;

    public Task() {

    }

    public Task(String title, String description, User user, Section section, String jiraId, Category category) {
        this.title = title;
        this.description = description;
        this.finishedAt = null;
        this.user = user;
        this.section = section;
        this.completed = false;
        Task.this.jiraId = jiraId;
        this.category = category;
        this.subtasks = new ArrayList<>();
        this.active = true;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    @JsonProperty("tabId")
    public UUID getTabId() {
        if (section != null && section.getTab() != null) {
            return section.getTab().getId();
        }
        return tab != null ? tab.getId() : null;
    }

    @JsonProperty("sectionId")
    public UUID getSectionId() {
        return section != null ? section.getId() : null;
    }

    public LocalDate getFinishDate(){
        return finishedAt;
    }

    public void setFinishDate(LocalDate finishedAt){
        this.finishedAt = finishedAt;
    }

    public String getJiraId() {
        return jiraId;
    }

    public void setJiraId(String jiraId) {
        this.jiraId = jiraId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getFinishedAt() {
        return finishedAt;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isArchived() {
        return !active;
    }

    public void setArchived(boolean archived) {
        this.active = !archived;
    }
}
