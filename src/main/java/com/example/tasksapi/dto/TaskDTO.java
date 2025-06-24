package com.example.tasksapi.dto;

import java.time.LocalDateTime;

public class TaskDTO {
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createDate;
    private LocalDateTime finishDate;
    private String jiraId;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
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

}
