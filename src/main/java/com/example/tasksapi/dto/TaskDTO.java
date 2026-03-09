package com.example.tasksapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskDTO {
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createDate;
    private LocalDateTime finishDate;
    private String jiraId;
    private UUID tabId;
    private UUID categoryId;

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

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public LocalDateTime getFinishDate(){
        return finishDate;
    }

    public String getJiraId() {
        return jiraId;
    }

    public void setJiraId(String jiraId) {
        this.jiraId = jiraId;
    }

    public UUID getTabId() {
        return tabId;
    }

    public void setTabId(UUID tabId) {
        this.tabId = tabId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

}
