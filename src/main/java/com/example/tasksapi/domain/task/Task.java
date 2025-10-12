package com.example.tasksapi.domain.task;

import com.example.tasksapi.domain.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "task",
    indexes = {
            @Index(name = "idx_task_user", columnList = "user_id"),
            @Index(name = "idx_task_tab", columnList = "tab_id"),
            @Index(name = "idx_task_archive", columnList = "active, archived_at, archive_expires_at")
    },
    uniqueConstraints = {
            @UniqueConstraint(name = "ux_task_user_sort", columnNames = {"user_id", "tab_id" , "sort_order"})
    }
)
public class Task extends Auditable {
    @Id @GeneratedValue
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tab_id", foreignKey = @ForeignKey(name = "fk_task_tab"))
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

    @Column(name = "archived_at")
    OffsetDateTime archivedAt;

    @Column(name = "archive_expires_at")
    OffsetDateTime archiveExpiresAt;

    public Task() {

    }

    public Task(String title, String description, User user, String jiraId, Category category) {
        this.title = title;
        this.description = description;
        this.finishedAt = null;
        this.user = user;
        this.completed = false;
        Task.this.jiraId = jiraId;
        this.category = category;
        this.subtasks = new ArrayList<>();
        this.active = true;
    }

    public void archiveForMonths(short months) {
        this.active = false;
        this.archivedAt = OffsetDateTime.now();
        this.archiveExpiresAt = this.archivedAt.plusMonths(months);
        this.tab = null; // solta vinculo com a tab;
    }

    public boolean isArchivedExpired(OffsetDateTime now){
        return !this.active && this.archiveExpiresAt != null && !now.isBefore(this.archiveExpiresAt);
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

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public OffsetDateTime getArchiveExpiresAt() {
        return archiveExpiresAt;
    }

    public void setArchiveExpiresAt(OffsetDateTime archiveExpiresAt) {
        this.archiveExpiresAt = archiveExpiresAt;
    }
}
