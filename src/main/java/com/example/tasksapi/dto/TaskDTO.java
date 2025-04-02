package com.example.tasksapi.dto;

import java.time.LocalDateTime;

public class TaskDTO {
    private String title;
    private String description;
    private boolean completed;



    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

}
